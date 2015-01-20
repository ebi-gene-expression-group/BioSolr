package uk.co.flax.biosolr.pdbe;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import uk.ac.ebi.webservices.axis1.stubs.fasta.InputParameters;
import uk.ac.ebi.webservices.axis1.stubs.fasta.JDispatcherService_PortType;
import uk.ac.ebi.webservices.axis1.stubs.fasta.WsResultType;

public class TestFastaJob {

	@Test
	public void parse() throws IOException, URISyntaxException {
		byte[] result = Files.readAllBytes(Paths.get(TestFastaJob.class.getResource("result").toURI()));
		
		JDispatcherService_PortType fasta = mock(JDispatcherService_PortType.class);
		when(fasta.getStatus(null)).thenReturn(FastaStatus.DONE);
		WsResultType[] types = new WsResultType[] { mock(WsResultType.class) };
		when(fasta.getResultTypes(null)).thenReturn(types);
		when(fasta.getResult(null, null, null)).thenReturn(result);
		
		InputParameters params = new InputParameters();
		params.setProgram("ssearch");
		params.setDatabase(new String[] { "pdb" });
		params.setStype("protein");
        params.setSequence("<DUMMY>");
        params.setExplowlim(0.0d);
        params.setExpupperlim(1.0d);
        params.setScores(1000);
        params.setAlignments(1000);
		
		FastaJob job = new FastaJob(fasta, "sameer@ebi.ac.uk", params);
		job.run();
		FastaJobResults results = job.getResults();
		
		assertEquals(1000, results.getNumChains());
		assertEquals(317, results.getNumEntries());
		
		List<String> order = results.getResultOrder();
		Map<String, Alignment> alignments = results.getAlignments();

		String pdbIdChain = order.get(0);
		String sequence = alignments.get(pdbIdChain).getReturnSequenceString();
		assertEquals("1CZI_E", pdbIdChain);
		
		assertEquals("GEVASVPLTNYLDSQYFGKIYLGTPPQEFTVLFDTGSSDFWVPSIYCKSNACKNHQRFDPRKSSTFQNLG" +
					 "KPLSIHYGTGSMQGILGYDTVTVSNIVDIQQTVGLSTQEPGDVFTYAEFDGILGMAYPSLASEYSIPVFD" +
					 "NMMNRHLVAQDLFSVYMDRNGQESMLTLGAIDPSYYTGSLHWVPVTVQQYWQFTVDSVTISGVVVACEGG" +
					 "CQAILDTGTSKLVGPSSDILNIQQAIGATQNQYGEFDIDCDNLSYMPTVVFEINGKMYPLTPSAYTSQDQ" +
					 "GFCTSGFQSENHSQKWILGDVFIREYYSVFDRANNLVGLAKAIGEVASVPLTNYLDSQYFGKIYLGTPPQ" +
					 "EFTVLFDTGSSDFWVPSIYCKSNACKNHQRFDPRKSSTFQNLGKPLSIHYGTGSMQGILGYDTVTVSNIV" +
					 "DIQQTVGLSTQEPGDVFTYAEFDGILGMAYPSLASEYSIPVFDNMMNRHLVAQDLFSVYMDRNGQESMLT" +
					 "LGAIDPSYYTGSLHWVPVTVQQYWQFTVDSVTISGVVVACEGGCQAILDTGTSKLVGPSSDILNIQQAIG" +
					 "ATQNQYGEFDIDCDNLSYMPTVVFEINGKMYPLTPSAYTSQDQGFCTSGFQSENHSQKWILGDVFIREYY" +
					 "SVFDRANNLVGLAKAI", sequence);
		
		assertEquals("3PWW,3PGI,1GKT,3ER3,1LEE,3ER5,3CMS,6APR,2RMP,2IKU,4K9H,1APW,4CKU,1APV," +
					 "3URI,1UH9,1APU,3URJ,1UH8,1APT,1UH7,3URL,2G94,3GW5,3ZL7,4OBZ,3ZKX,1ZAP," +
					 "3VSX,1LF2,3ZKM,3ZKN,3VCM,3ZKQ,4OC6,3ZKS,1E82,1E81,1WKR,1E80,3ZKG,1XN3," +
					 "1XN2,3ZKI,1LF4,3VSW,1LF3,3G6Z,4OD9,3FV3,3VUC,3OWN,4FS4,3ZLQ,1BBS,3G70," +
					 "3G72,2ANL,3PI0,4PEP,3UTL,2EWY,1FLH,4ER1,1QRP,4ER2,1PPM,4ER4,1PPL,4CMS," +
					 "1PPK,1MIQ,3ZMG,2APR,1FMU,1LYW,4RLD,1IBQ,1QS8,1LYB,1LYA,4LP9,4PYV,1SGZ," +
					 "1HRN,3PLL,3K1W,1TZS,3PLD,1B5F,3ZOV,1FMX,3O9L,5PEP,1J71,3BRA,3VYF,5ER2," +
					 "1G0V,1PSO,1PSN,3UFL,3U6A,3VYD,5ER1,3VYE,1PSA,3PM4,1XS7,1QDM,2VKM,4LAP," +
					 "2X0B,1HTR,4GJ5,4GJ6,3APR,1FQ8,4AMT,1FQ7,1FQ6,1ENT,1FQ5,1FQ4,3PMY,2ASI," +
					 "4LBT,3F9Q,1CZI,3APP,4GID,1AVF,1BXQ,3PMU,1BXO,1XE5,1BIM,1BIL,3T6I,1YG9," +
					 "1XE6,4GJA,4GJB,4GJC,4GJD,4GJ7,4GJ8,4GJ9,1XDH,2REN,1EPR,3T7P,1EPQ,4J0T," +
					 "2I4Q,1EPP,3T7Q,3QS1,1EPO,4J17,4J0V,2PSG,3SFC,1EPN,1EPM,1EPL,4J0Y,1MPP," +
					 "4J0P,4EWO,4B0Q,1OD1,1YX9,2G24,2G1S,1F34,2G1R,2G22,2G21,2G20,2G1O,1PFZ," +
					 "2G1N,2JXR,4J1K,2G1Y,2G27,1SME,1EAG,1ER8,2G26,1CMS,1DP5,4J1C,3D91,4J1E," +
					 "4J1F,4J1H,4J1I,4APR,4B1C,4J0Z,3BUF,4EXG,3BUG,4B1D,3QRV,4APE,3T7X,3BUH," +
					 "4B1E,3FNS,1GVW,3LZY,3FNT,1GVV,3FNU,1GVU,1GVT,2P4J,3PRS,1OEX,3Q3T,3PBD," +
					 "1OEW,3PB5,4KUP,1GVX,1SMR,3LIZ,1IZE,1IZD,3OOT,1DPJ,4AA8,2V00,4L6B,4AA9," +
					 "2JJI,4LHH,2FS4,4Q1N,3EMY,3OAD,3PSG,3OQF,2BJU,3PBZ,3OQK,1W6I,1W6H,2JJJ," +
					 "2V16,3CIB,3CIC,3CID,3Q4B,2V0Z,2NR6,2V10,2V11,2V12,2V13,2IGY,2R9B,2IGX," +
					 "2ER6,2H6T,2H6S,2ER0,3Q5H,3QVI,3PCW,1LS5,1M43,3PCZ,4BEK,2ER9,4BEL,2BKT," +
					 "1AM5,2BKS,1RNE,2ER7,3QVC,5APR,3C9X,3OAG,3PSY,2VS2,4AUC,1EED,2QZX,2QZW," +
					 "3Q70,4BFB,4BFD,2WEA,1ME6,2WEB,2WEC,2WED,3TNE,3KM4,2IL2,3PVK,4K8S,2IKO," +
					 "3PEP,3IXJ,4B72,4B77,4B78,2FDP,3Q6Y,1E5O,4B70", results.getPdbIdCodes());
		
		String idChains = String.join(",", results.getResultOrder());
		assertEquals("1CZI_E,1CZI_E,4AUC_A,1CMS_A,4CMS_A,4CMS_A,1CMS_A,4AUC_A,4AA8_A,4AA8_A," +
					 "3CMS_A,3CMS_A,4AA9_A,4AA9_A,1QRP_E,1PSN_A,1PSO_E,1PSO_E,1FLH_A,1PSN_A," +
					 "1FLH_A,3UTL_A,1QRP_E,3UTL_A,5PEP_A,5PEP_A,1F34_A,1PSA_B,1PSA_B,1YX9_A," +
					 "1YX9_A,1F34_A,1PSA_A,1PSA_A,3PEP_A,4PEP_A,4PEP_A,3PEP_A,3PSG_A,3PSG_A," +
					 "2PSG_A,2PSG_A,1TZS_A,1TZS_A,1AM5_A,1AM5_A,1HTR_B,1AVF_J,1AVF_A,1HTR_B," +
					 "1AVF_J,1AVF_A,1SMR_E,1SMR_A,1SMR_G,1SMR_C,1SMR_E,1SMR_C,1SMR_G,1SMR_A," +
					 "3VCM_B,3VCM_A,3VCM_A,3VCM_B,2G24_B,2G1N_A,2G20_A,2G1S_A,2G1R_A,2G26_B," +
					 "2G21_B,2G26_B,2G1Y_A,2G1S_A,2FS4_A,2G1N_A,2G1S_B,2G21_A,2FS4_B,2G20_A," +
					 "2G27_A,2G1Y_A,2G1S_B,2G1N_B,2G21_A,2FS4_A,2FS4_B,2G22_B,2G20_B,2G1O_B," +
					 "2G24_A,2G1Y_B,2G1R_B,2G22_B,2G1O_A,2G27_B,2G27_A,2G22_A,2G26_A,2G1O_B," +
					 "2G24_A,2G27_B,2G1N_B,2G1R_B,2G1Y_B,2G21_B,2G26_A,2G22_A,2G1R_A,2G20_B," +
					 "2G24_B,2G1O_A,2I4Q_A,2I4Q_A,2I4Q_B,2I4Q_B,1BIL_A,1BIM_B,3KM4_B,3GW5_A," +
					 "3KM4_B,3KM4_A,1HRN_A,1HRN_B,1BIL_B,3GW5_B,1BIL_B,1BIL_A,1HRN_B,1BIM_B," +
					 "3GW5_A,3KM4_A,1BIM_A,1BIM_A,1HRN_A,3GW5_B,3VYD_A,3VUC_B,3VSX_B,4GJ8_B," +
					 "3OOT_A,3VSX_A,2BKS_A,2V0Z_C,2V0Z_C,4GJA_A,3OQF_A,3Q5H_B,3Q4B_A,2BKS_A," +
					 "2REN_A,3VYD_B,2V13_A,1BBS_A,4GJ5_A,3OOT_B,2IL2_A,3OQF_B,3G72_A,2IKU_B," +
					 "2IKU_B,2V12_C,3Q4B_B,2V16_C,4GJD_B,3Q3T_A,4GJB_A,2BKT_A,4GJC_B,3OQK_A," +
					 "2V11_O,4GJA_B,2IKO_A,2IL2_A,4GJB_B,2V16_O,2BKT_A,4GJ5_B,3OOT_B,4GJ9_B," +
					 "2V11_C,4GJD_B,3VYD_A,2V13_A,3G72_B,4GJD_A,4GJB_A,2IKU_A,3VYE_A,3VSW_A," +
					 "3Q4B_A,4Q1N_B,2IKO_A,3VSW_B,2IKO_B,4PYV_B,3OQK_B,3Q3T_B,3OQK_B,2BKS_B," +
					 "3VUC_A,2IL2_B,1BBS_B,2REN_A,4PYV_A,4GJ9_A,2V16_C,3OQF_B,2V10_C,4GJA_A," +
					 "4GJA_B,3OOT_A,1BBS_A,3VYE_A,4GJ9_B,4GJC_B,2BKS_B,3VYE_B,4GJ7_B,4GJC_A," +
					 "2IKU_A,1RNE_A,3Q5H_A,3VYF_A,3VUC_A,3SFC_B,4GJ6_A,4GJ5_A,4GJ8_B,4GJ9_A," +
					 "2V12_O,2V0Z_O,4GJ8_A,4Q1N_A,3SFC_B,3OQF_A,4GJB_B,3VYE_B,3G72_A,2IKO_B," +
					 "4GJD_A,3VYD_B,4PYV_A,2V0Z_O,4GJ7_A,3Q3T_A,2V10_C,4GJC_A,2V16_O,4Q1N_A," +
					 "3SFC_A,3VYF_B,4GJ7_A,2IL2_B,3Q5H_A,3VSX_A,2V12_O,4GJ5_B,1BBS_B,4PYV_B," +
					 "3VYF_A,3OQK_A,3VSW_A,2V11_C,4GJ6_B,3Q3T_B,2BKT_B,1RNE_A,3VUC_B,2V10_O," +
					 "4GJ7_B,3SFC_A,3VSX_B,2V11_O,3Q5H_B,3Q4B_B,2V12_C,2BKT_B,2V10_O,3G72_B," +
					 "3VSW_B,4GJ6_B,4GJ6_A,4Q1N_B,4GJ8_A,3VYF_B,3OWN_B,3G70_B,3OWN_A,3OWN_A," +
					 "3G70_A,3D91_B,3K1W_B,3G70_B,3D91_B,3D91_A,3G70_A,3G6Z_B,3G6Z_B,3K1W_A," +
					 "3G6Z_A,3K1W_A,3K1W_B,3OWN_B,3G6Z_A,3D91_A,2X0B_C,2X0B_G,2X0B_E,2X0B_E," +
					 "4AMT_A,2X0B_A,2X0B_C,4AMT_A,2X0B_A,2X0B_G,1G0V_A,1G0V_A,1FMU_A,1FMX_B," +
					 "1DPJ_A,1FMX_A,1FMU_A,1DP5_A,1FMX_A,1DP5_A,1FMX_B,1DPJ_A,1FQ4_A,1FQ8_A," +
					 "1FQ6_A,1FQ5_A,2JXR_A,2JXR_A,1FQ6_A,1FQ8_A,1FQ7_A,1FQ5_A,1FQ4_A,1FQ7_A," +
					 "1QDM_A,1QDM_C,1QDM_A,1QDM_B,1QDM_C,1QDM_B,1LYW_B,1LYB_B,1LYB_D,1LYW_D," +
					 "1LYB_D,1LYA_B,1LYA_B,1LYA_D,1LYW_B,1LYW_F,1LYW_F,1LYW_H,1LYW_D,1LYW_H," +
					 "1LYA_D,1LYB_B,4OD9_B,4OBZ_D,4OD9_D,4OD9_D,4OBZ_B,4OD9_B,4OC6_B,4OBZ_B," +
					 "4OC6_B,4OBZ_D,1B5F_C,1B5F_C,1B5F_A,1B5F_A,5APR_E,3APR_E,2APR_A,2APR_A," +
					 "3APR_E,5APR_E,6APR_E,6APR_E,4APR_E,4APR_E,1UH7_A,1UH7_A,1UH8_A,1UH9_A," +
					 "1UH9_A,1UH8_A,3QRV_A,3QS1_A,3QS1_D,3QRV_B,3QS1_B,3QS1_C,3QS1_B,3QS1_D," +
					 "3QS1_A,3QRV_B,3QS1_C,3QRV_A,1XE5_A,1XE6_B,2IGY_A,2IGX_A,1XE5_A,1ME6_B," +
					 "2IGX_A,1ME6_A,1XE6_A,1SME_A,2R9B_A,2R9B_B,1XE6_A,1SME_B,1XE6_B,2IGY_A," +
					 "1ME6_A,2IGY_B,2IGY_B,2R9B_B,2R9B_A,1XE5_B,1SME_A,1XE5_B,1SME_B,1ME6_B," +
					 "1W6I_A,1XDH_B,1W6I_A,1LF4_A,1W6I_C,1W6H_A,1W6H_B,1W6H_B,1LF3_A,1W6H_A," +
			 		 "1XDH_B,1LF4_A,1LF3_A,1XDH_A,1XDH_A,1W6I_C,1PFZ_A,1PFZ_C,1PFZ_D,1PFZ_B," +
					 "1PFZ_C,1PFZ_A,1PFZ_D,1PFZ_B,2BJU_A,2BJU_A,4CKU_E,4CKU_D,4CKU_F,4CKU_B," +
			 		 "4CKU_D,4CKU_B,3F9Q_A,4CKU_A,3F9Q_A,4CKU_E,4CKU_C,4CKU_C,4CKU_F,4CKU_A," +
					 "1M43_A,1M43_B,1LF2_A,1LEE_A,1LF2_A,1LEE_A,1M43_A,1M43_B,2ANL_A,2ANL_B," +
			 		 "2ANL_A,2ANL_B,1QS8_A,1QS8_A,1QS8_B,1QS8_B,1MIQ_A,1MIQ_B,1MIQ_B,1MIQ_A," +
					 "3LIZ_A,3LIZ_A,1LS5_B,1LS5_A,1LS5_A,1LS5_B,2NR6_A,1YG9_A,4RLD_Entity,1YG9_A," +
			 		 "2NR6_B,2NR6_A,2NR6_B,3OAD_A,3OAD_A,3O9L_A,3OAG_A,3OAG_A,3OAD_C,3O9L_C," +
					 "3OAG_C,3O9L_C,3OAG_C,3O9L_A,3OAD_C,1MPP_A,1MPP_A,2RMP_A,2ASI_A,2RMP_A," +
			 		 "2ASI_A,3FV3_H,3FV3_C,3FV3_G,3FV3_D,3FV3_A,3FV3_E,3FV3_B,3FV3_F,3FV3_H," +
					 "3TNE_A,3FV3_D,3FV3_E,3FV3_F,3FV3_B,3FV3_G,3FV3_C,3FV3_A,3TNE_A,3TNE_B," +
			 		 "3TNE_B,3FNU_D,3FNU_D,3FNS_B,3FNU_A,3FNU_A,3FNT_A,3FNS_A,3FNU_B,3FNU_B," +
					 "3FNT_A,3FNS_B,3FNS_A,3FNU_C,3FNU_C,2H6T_A,2H6T_A,2H6S_A,2H6S_A,3QVI_D," +
			 		 "3QVC_A,3QVC_A,3QVI_B,3QVI_A,3QVI_C,3QVI_C,3QVI_B,3QVI_A,3QVI_D,2QZW_A," +
					 "2QZW_B,2QZW_B,2QZW_A,1ZAP_A,1ZAP_A,1EAG_A,3PVK_A,3Q70_A,3PVK_A,3Q70_A," +
			 		 "1EAG_A,1APT_E,2WEC_A,1BXO_A,1APV_E,1PPK_E,3APP_A,1APU_E,1BXQ_A,2WEA_A," +
					 "1APW_E,3APP_A,2WEB_A,2WED_A,1APT_E,1PPL_E,1APW_E,1APU_E,2WEB_A,2WED_A," +
			 		 "1PPM_E,1APV_E,2WEC_A,1PPK_E,1PPM_E,1BXQ_A,1BXO_A,2WEA_A,1PPL_E,3EMY_A," +
					 "3EMY_A,3C9X_A,3C9X_A,1J71_A,1J71_A,1IBQ_B,1IBQ_A,1IBQ_B,1IBQ_A,1GVU_A," +
			 		 "3PB5_A,3PCZ_A,3PWW_A,3PRS_A,2ER7_E,1GVW_A,4ER1_E,4APE_A,3Q6Y_A,1E82_E," +
					 "1EPM_E,4LAP_Entity,1GVT_A,5ER1_E,2V00_A,1EPP_E,3T7P_A,3LZY_A,1EPN_E,2ER9_E," +
			 		 "4L6B_A,3PLD_A,4ER1_E,2V00_A,3PCZ_A,2ER6_E,3PGI_A,1GVX_A,3ER5_E,3T7Q_A," +
					 "3PBZ_A,3PBD_A,4ER4_E,2JJJ_A,4KUP_A,3T6I_A,1EPQ_E,5ER1_E,1EPL_E,1EPM_E," +
			 		 "1EPN_E,1EPL_E,2ER9_E,1GVX_A,1E5O_E,1GKT_A,3PSY_A,2VS2_A,3PRS_A,3URL_A," +
					 "2ER0_E,4ER2_E,3PMY_A,1GVV_A,3PCW_A,3PLL_A,3PMU_A,1EED_P,1GKT_A,1EPO_E," +
			 		 "1OEX_A,3URL_A,3PM4_A,5ER2_E,1E81_E,2ER6_E,3T7Q_A,4LP9_A,1EPO_E,3LZY_A," +
					 "1E82_E,3PCW_A,4ER4_E,1EPR_E,2VS2_A,1E80_E,3URJ_A,1GVW_A,3PB5_A,1GVT_A," +
			 		 "1ENT_E,3PLL_A,1OEW_A,3PMU_A,1OD1_A,1OEW_A,3PI0_A,1GVU_A,3PBZ_A,1EED_P," +
					 "3PSY_A,2ER7_E,4ER2_E,1EPP_E,2JJI_A,3T6I_A,3ER3_E,3PMY_A,3URI_A,3PGI_A," +
					 "3T7P_A,1ER8_E,3PM4_A,4LBT_A,3T7X_A,1OD1_A,2JJJ_A,1E80_E,4LBT_A,1EPQ_E," +
					 "1EPR_E,4LP9_A,3Q6Y_A,5ER2_E,3URI_A,4L6B_A,4APE_A,1E81_E,3T7X_A,4LHH_A," +
					 "3ER5_E,3PBD_A,3ER3_E,3URJ_A,3PWW_A,1ER8_E,2JJI_A,2ER0_E,1OEX_A,4LHH_A," +
					 "3PLD_A,1GVV_A,3PI0_A,4KUP_A,1E5O_E,1ENT_E,1WKR_A,1WKR_A,2QZX_A,2QZX_A," +
					 "2QZX_B,2QZX_B,1LYA_C,1LYB_A,1LYW_A,1LYB_C,1LYW_E,1LYW_G,1LYW_A,1LYA_C," +
					 "1LYB_A,1LYA_A,1LYB_C,1LYW_C,1LYW_G,1LYA_A,1LYW_E,1LYW_C,4OD9_A,4OBZ_C," +
					 "4OC6_A,4OD9_A,4OBZ_A,4OBZ_C,4OD9_C,4OD9_C,4OBZ_A,4OC6_A,3OAD_D,3O9L_D," +
					 "3O9L_B,3OAG_D,3OAG_B,3OAD_B,3OAD_B,3OAD_D,3OAG_D,3OAG_B,3O9L_D,3O9L_B," +
					 "4B1C_A,4B1C_A,1IZE_A,1IZD_A,1IZD_A,1IZE_A,2EWY_A,2EWY_C,2EWY_B,2EWY_C," +
					 "2EWY_D,2EWY_D,2EWY_A,2EWY_B,3ZKS_A,3ZKN_B,4BEL_B,4BEL_A,3ZKM_B,4BFB_A," +
					 "4BEL_B,3ZKM_B,3ZKQ_A,4BFB_B,4BEL_A,4BFB_B,3ZKM_A,4BFB_A,3ZKS_A,3ZKN_A," +
					 "3ZKN_A,3ZKM_A,3ZKQ_A,3ZKN_B,3ZKX_A,3ZL7_A,3ZLQ_A,3ZKI_B,3ZLQ_B,3ZKG_A," +
					 "3ZKI_B,3ZLQ_A,3ZL7_A,3ZKG_B,3ZKX_A,3ZKG_A,3ZKI_A,3ZKG_B,3ZKI_A,3ZLQ_B," +
					 "4J1C_A,4J0Z_A,4J0P_A,4BFD_A,3ZMG_A,4J1F_A,4J1K_A,4J1E_A,4J17_A,3BUG_A," +
					 "4J0P_A,4J0Z_A,4J0V_A,4J0V_A,4J1H_A,3ZOV_A,4J1C_A,3BUF_A,3BUF_A,4J1F_A," +
					 "3BRA_A,4BEK_A,3BRA_A,3BUG_A,4J1H_A,4J1I_A,4J0Y_A,4BEK_A,4BFD_A,3ZMG_A," +
					 "3BUH_A,3BUH_A,4J0Y_A,4J0T_A,3ZOV_A,4J0T_A,4J17_A,4J1I_A,4J1E_A,4J1K_A," +
					 "4B78_A,4B78_A,4B0Q_A,4B0Q_A,4B70_A,4B70_A,4EWO_A,4EXG_A,4EXG_A,4EWO_A," +
					 "4K9H_A,4K8S_C,4K8S_A,4GID_D,3IXJ_C,4K8S_A,4B1E_A,4B72_A,4K8S_C,4GID_B," +
					 "4K8S_B,2FDP_A,4K9H_A,4GID_A,4K9H_C,2FDP_B,4B1D_A,4B1E_A,4GID_B,4K9H_B," +
					 "4GID_D,4B77_A,4B77_A,4B1D_A,3IXJ_B,4K9H_C,4K8S_B,3IXJ_A,4GID_C,4GID_C," +
					 "2FDP_C,4B72_A,2FDP_A,4K9H_B,4GID_A,3IXJ_C,2FDP_C,3IXJ_B,2FDP_B,3IXJ_A," +
					 "1XN3_C,2VKM_B,1XN2_C,2G94_C,1XS7_D,1SGZ_D,1XN2_D,2VKM_B,2P4J_C,2G94_A," +
					 "1SGZ_C,2VKM_D,1XN3_D,2G94_B,1XN2_D,3UFL_A,1XN2_C,1SGZ_A,1XS7_D,1XN3_B," +
					 "2VKM_C,1SGZ_D,1XN2_A,1SGZ_B,1XN3_C,2G94_C,2P4J_B,2G94_B,3UFL_A,2P4J_B," +
					 "1XN3_A,2VKM_A,2VKM_D,2P4J_D,2VKM_A,2P4J_D,1XN3_D,1SGZ_B,2P4J_A,2G94_A," +
					 "1SGZ_A,2G94_D,2G94_D,1XN3_B,2P4J_C,1XN2_B,1XN3_A,1SGZ_C,2P4J_A,1XN2_A," +
					 "2VKM_C,1XN2_B,3CIB_A,3CIC_B,4FS4_B,3U6A_A,3CIC_A,3CID_A,3CIB_B,3CID_A", idChains);
	}
	
}
