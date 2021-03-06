/*
 * Copyright (C) 2015 University of Pittsburgh.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package edu.pitt.dbmi.ccd.web.ctrl.algo;

import edu.pitt.dbmi.ccd.db.service.AlgorithmRunLogService;
import edu.pitt.dbmi.ccd.db.service.DataFileService;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.model.AppUser;
import edu.pitt.dbmi.ccd.web.model.algo.AlgorithmJobRequest;
import edu.pitt.dbmi.ccd.web.model.algo.CommonGFCIAlgoOpt;
import edu.pitt.dbmi.ccd.web.model.algo.GFCIcAlgoOpt;
import edu.pitt.dbmi.ccd.web.model.algo.GFCIdAlgoOpt;
import edu.pitt.dbmi.ccd.web.model.algo.GFCImCGAlgoOpt;
import edu.pitt.dbmi.ccd.web.prop.CcdProperties;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
import edu.pitt.dbmi.ccd.web.service.algo.AlgorithmRunService;
import edu.pitt.dbmi.ccd.web.util.TetradCmdOptions;
import static edu.pitt.dbmi.ccd.web.util.TetradCmdOptions.PENALTY_DISCOUNT;
import static edu.pitt.dbmi.ccd.web.util.TetradCmdOptions.STRUCTURE_PRIOR;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * May 28, 2017 9:50:44 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "algorithm/gfci")
public class GFCIController extends AbstractTetradAlgoController implements ViewPath, TetradCmdOptions {

    private final String GFCIC_ALGO_NAME = "gfcic";
    private final String GFCID_ALGO_NAME = "gfcid";
    private final String GFCIM_CG_ALGO_NAME = "gfcim-cg";

    private final AlgorithmRunLogService algorithmRunLogService;
    private final AlgorithmRunService algorithmRunService;
    private final CcdProperties ccdProperties;

    @Autowired
    public GFCIController(AlgorithmRunLogService algorithmRunLogService, AlgorithmRunService algorithmRunService, CcdProperties ccdProperties, DataFileService dataFileService, AppUserService appUserService) {
        super(dataFileService, appUserService);
        this.algorithmRunLogService = algorithmRunLogService;
        this.algorithmRunService = algorithmRunService;
        this.ccdProperties = ccdProperties;
    }

    @RequestMapping(value = "gfcim_cg", method = RequestMethod.POST)
    public String runGFCImCG(
            @ModelAttribute("algoOpt") final GFCImCGAlgoOpt algoOpt,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        AlgorithmJobRequest jobRequest = new AlgorithmJobRequest("GFCIm-CG", ccdProperties.getAlgoJar(), ccdProperties.getAlgoGfcimCGS());
        jobRequest.setDataset(getDataset(algoOpt));
        jobRequest.setPriorKnowledge(getPriorKnowledge(algoOpt));
        jobRequest.setJvmOptions(getJvmOptions(algoOpt));
        jobRequest.setParameters(getParametersForMixedCG(algoOpt, appUser.getUsername()));

        algorithmRunService.addToQueue(jobRequest, appUser.getUsername());
        algorithmRunLogService.logAlgorithmRun(getGFCImCGParams(algoOpt), getFileSummary(algoOpt, appUser), GFCIM_CG_ALGO_NAME, appUser.getUsername());

        return REDIRECT_JOB_QUEUE;
    }

    @RequestMapping(value = "gfcim_cg", method = RequestMethod.GET)
    public String showGFCImCGView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        Map<String, String> dataset = algorithmRunService.getUserMixedDataset(appUser.getUsername());
        Map<String, String> prior = algorithmRunService.getUserPriorKnowledgeFiles(appUser.getUsername());
        GFCImCGAlgoOpt algoOpt = new GFCImCGAlgoOpt();

        // set the default dataset
        if (!dataset.isEmpty()) {
            algoOpt.setDataset(dataset.keySet().iterator().next());  // get one element
        }

        if (!prior.isEmpty()) {
            algoOpt.setPriorKnowledge(prior.keySet().iterator().next());
        }

        model.addAttribute("datasetList", dataset);
        model.addAttribute("priorList", prior);
        model.addAttribute("algoOpt", algoOpt);

        return GFCI_MIXED_CG_VIEW;
    }

    @RequestMapping(value = "gfcid", method = RequestMethod.POST)
    public String runGfciDiscrete(
            @ModelAttribute("algoOpt") final GFCIdAlgoOpt algoOpt,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        AlgorithmJobRequest jobRequest = new AlgorithmJobRequest("GFCId", ccdProperties.getAlgoJar(), ccdProperties.getAlgoGfciDisc());
        jobRequest.setDataset(getDataset(algoOpt));
        jobRequest.setPriorKnowledge(getPriorKnowledge(algoOpt));
        jobRequest.setJvmOptions(getJvmOptions(algoOpt));
        jobRequest.setParameters(getParametersForDiscrete(algoOpt, appUser.getUsername()));

        algorithmRunService.addToQueue(jobRequest, appUser.getUsername());
        algorithmRunLogService.logAlgorithmRun(getGFCIdParams(algoOpt), getFileSummary(algoOpt, appUser), GFCID_ALGO_NAME, appUser.getUsername());

        return REDIRECT_JOB_QUEUE;
    }

    @RequestMapping(value = "gfcid", method = RequestMethod.GET)
    public String showGfciDiscreteView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        Map<String, String> dataset = algorithmRunService.getUserDiscreteDataset(appUser.getUsername());
        Map<String, String> prior = algorithmRunService.getUserPriorKnowledgeFiles(appUser.getUsername());
        GFCIdAlgoOpt algoOpt = new GFCIdAlgoOpt();

        // set the default dataset
        if (!dataset.isEmpty()) {
            algoOpt.setDataset(dataset.keySet().iterator().next());  // get one element
        }

        if (!prior.isEmpty()) {
            algoOpt.setPriorKnowledge(prior.keySet().iterator().next());
        }

        model.addAttribute("datasetList", dataset);
        model.addAttribute("priorList", prior);
        model.addAttribute("algoOpt", algoOpt);

        return GFCI_DISC_VIEW;
    }

    @RequestMapping(value = "gfcic", method = RequestMethod.POST)
    public String runGfciContinuous(
            @ModelAttribute("algoOpt") final GFCIcAlgoOpt algoOpt,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        AlgorithmJobRequest jobRequest = new AlgorithmJobRequest("GFCIc", ccdProperties.getAlgoJar(), ccdProperties.getAlgoGfciCont());
        jobRequest.setDataset(getDataset(algoOpt));
        jobRequest.setPriorKnowledge(getPriorKnowledge(algoOpt));
        jobRequest.setJvmOptions(getJvmOptions(algoOpt));
        jobRequest.setParameters(getParametersForContinuous(algoOpt, appUser.getUsername()));

        algorithmRunService.addToQueue(jobRequest, appUser.getUsername());
        algorithmRunLogService.logAlgorithmRun(getGFCIcParams(algoOpt), getFileSummary(algoOpt, appUser), GFCIC_ALGO_NAME, appUser.getUsername());

        return REDIRECT_JOB_QUEUE;
    }

    @RequestMapping(value = "gfcic", method = RequestMethod.GET)
    public String showGfciContinuousView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        Map<String, String> dataset = algorithmRunService.getUserContinuousDataset(appUser.getUsername());
        Map<String, String> prior = algorithmRunService.getUserPriorKnowledgeFiles(appUser.getUsername());
        GFCIcAlgoOpt algoOpt = new GFCIcAlgoOpt();

        // set the default dataset
        if (!dataset.isEmpty()) {
            algoOpt.setDataset(dataset.keySet().iterator().next());  // get one element
        }

        if (!prior.isEmpty()) {
            algoOpt.setPriorKnowledge(prior.keySet().iterator().next());
        }

        model.addAttribute("datasetList", dataset);
        model.addAttribute("priorList", prior);
        model.addAttribute("algoOpt", algoOpt);

        return GFCI_CONT_VIEW;
    }

    private List<String> getParametersForContinuous(GFCIcAlgoOpt algoOpt, String username) {
        List<String> parameters = new LinkedList<>();
        String delimiter = algorithmRunService.getFileDelimiter(algoOpt.getDataset(), username);
        parameters.add(DELIMITER);
        parameters.add(delimiter);
        parameters.add(ALPHA);
        parameters.add(Double.toString(algoOpt.getAlpha()));
        parameters.add(PENALTY_DISCOUNT);
        parameters.add(Double.toString(algoOpt.getPenaltyDiscount()));
        parameters.add(MAX_DEGREE);
        parameters.add(Integer.toString(algoOpt.getMaxDegree()));
        parameters.add(MAX_PATH_LENGTH);
        parameters.add(Integer.toString(algoOpt.getMaxPathLength()));
        if (algoOpt.isFaithfulnessAssumed()) {
            parameters.add(FAITHFULNESS_ASSUMED);
        }
        if (algoOpt.isCompleteRuleSetUsed()) {
            parameters.add(COMPLETE_RULE_SET_USED);
        }
        if (algoOpt.isVerbose()) {
            parameters.add(VERBOSE);
        }
        if (algoOpt.isSkipNonZeroVariance()) {
            parameters.add(SKIP_NONZERO_VARIANCE);
        }
        if (algoOpt.isSkipUniqueVarName()) {
            parameters.add(SKIP_UNIQUE_VAR_NAME);
        }

        parameters.add(TETRAD_GRAPH_JSON);

        return parameters;
    }

    private List<String> getParametersForMixedCG(GFCImCGAlgoOpt algoOpt, String username) {
        List<String> parameters = new LinkedList<>();
        String delimiter = algorithmRunService.getFileDelimiter(algoOpt.getDataset(), username);
        parameters.add(DELIMITER);
        parameters.add(delimiter);
        parameters.add(ALPHA);
        parameters.add(Double.toString(algoOpt.getAlpha()));
        parameters.add(PENALTY_DISCOUNT);
        parameters.add(Double.toString(algoOpt.getPenaltyDiscount()));
        parameters.add(STRUCTURE_PRIOR);
        parameters.add(Double.toString(algoOpt.getStructurePrior()));
        parameters.add(NUM_CATEGORIES_TO_DISCRETIZE);
        parameters.add(Integer.toString(algoOpt.getNumCategoriesToDiscretize()));
        parameters.add(NUM_DISCRETE_CATEGORIES);
        parameters.add(Integer.toString(algoOpt.getNumberOfDiscreteCategories()));
        if (algoOpt.isDiscretize()) {
            parameters.add(DISCRETIZE);
        }
        parameters.add(MAX_DEGREE);
        parameters.add(Integer.toString(algoOpt.getMaxDegree()));
        parameters.add(MAX_PATH_LENGTH);
        parameters.add(Integer.toString(algoOpt.getMaxPathLength()));
        if (algoOpt.isFaithfulnessAssumed()) {
            parameters.add(FAITHFULNESS_ASSUMED);
        }
        if (algoOpt.isCompleteRuleSetUsed()) {
            parameters.add(COMPLETE_RULE_SET_USED);
        }
        if (algoOpt.isVerbose()) {
            parameters.add(VERBOSE);
        }

        parameters.add(TETRAD_GRAPH_JSON);

        return parameters;
    }

    private List<String> getParametersForDiscrete(GFCIdAlgoOpt algoOpt, String username) {
        List<String> parameters = new LinkedList<>();
        String delimiter = algorithmRunService.getFileDelimiter(algoOpt.getDataset(), username);
        parameters.add(DELIMITER);
        parameters.add(delimiter);
        parameters.add(ALPHA);
        parameters.add(Double.toString(algoOpt.getAlpha()));
        parameters.add(STRUCTURE_PRIOR);
        parameters.add(Double.toString(algoOpt.getStructurePrior()));
        parameters.add(SAMPLE_PRIOR);
        parameters.add(Double.toString(algoOpt.getSamplePrior()));
        parameters.add(MAX_DEGREE);
        parameters.add(Integer.toString(algoOpt.getMaxDegree()));
        parameters.add(MAX_PATH_LENGTH);
        parameters.add(Integer.toString(algoOpt.getMaxPathLength()));
        if (algoOpt.isFaithfulnessAssumed()) {
            parameters.add(FAITHFULNESS_ASSUMED);
        }
        if (algoOpt.isCompleteRuleSetUsed()) {
            parameters.add(COMPLETE_RULE_SET_USED);
        }
        if (algoOpt.isVerbose()) {
            parameters.add(VERBOSE);
        }
        if (algoOpt.isSkipCategoryLimit()) {
            parameters.add(SKIP_CATEGORY_LIMIT);
        }
        if (algoOpt.isSkipUniqueVarName()) {
            parameters.add(SKIP_UNIQUE_VAR_NAME);
        }

        parameters.add(TETRAD_GRAPH_JSON);

        return parameters;
    }

    private Map<String, String> getGFCImCGParams(GFCImCGAlgoOpt algoOpt) {
        Map<String, String> params = new HashMap<>();
        params.put(ALPHA.replaceAll("--", ""), Double.toString(algoOpt.getAlpha()));
        params.put(STRUCTURE_PRIOR.replaceAll("--", ""), Double.toString(algoOpt.getStructurePrior()));
        params.put(NUM_CATEGORIES_TO_DISCRETIZE.replaceAll("--", ""), Integer.toString(algoOpt.getNumCategoriesToDiscretize()));
        params.put(NUM_DISCRETE_CATEGORIES.replaceAll("--", ""), Integer.toString(algoOpt.getNumberOfDiscreteCategories()));
        params.put(DISCRETIZE.replaceAll("--", ""), algoOpt.isDiscretize() ? "true" : "false");

        getCommonGFCIParams(params, algoOpt);

        return params;
    }

    private Map<String, String> getGFCIdParams(GFCIdAlgoOpt algoOpt) {
        Map<String, String> params = new HashMap<>();
        params.put(ALPHA.replaceAll("--", ""), Double.toString(algoOpt.getAlpha()));
        params.put(SAMPLE_PRIOR.replaceAll("--", ""), Double.toString(algoOpt.getSamplePrior()));
        params.put(STRUCTURE_PRIOR.replaceAll("--", ""), Double.toString(algoOpt.getStructurePrior()));

        getCommonGFCIParams(params, algoOpt);

        return params;
    }

    private Map<String, String> getGFCIcParams(GFCIcAlgoOpt algoOpt) {
        Map<String, String> params = new HashMap<>();
        params.put(ALPHA.replaceAll("--", ""), Double.toString(algoOpt.getAlpha()));
        params.put(PENALTY_DISCOUNT.replaceAll("--", ""), Double.toString(algoOpt.getPenaltyDiscount()));

        getCommonGFCIParams(params, algoOpt);

        return params;
    }

    private void getCommonGFCIParams(Map<String, String> params, CommonGFCIAlgoOpt algoOpt) {
        params.put(MAX_DEGREE.replaceAll("--", ""), Integer.toString(algoOpt.getMaxDegree()));
        params.put(MAX_PATH_LENGTH.replaceAll("--", ""), Integer.toString(algoOpt.getMaxPathLength()));
        params.put(FAITHFULNESS_ASSUMED.replaceAll("--", ""), algoOpt.isFaithfulnessAssumed() ? "true" : "false");
        params.put(COMPLETE_RULE_SET_USED.replaceAll("--", ""), algoOpt.isCompleteRuleSetUsed() ? "true" : "false");
    }

}
