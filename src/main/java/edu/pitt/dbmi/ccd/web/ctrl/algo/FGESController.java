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

import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.model.AppUser;
import edu.pitt.dbmi.ccd.web.model.algo.AlgorithmJobRequest;
import edu.pitt.dbmi.ccd.web.model.algo.AlgorithmRunInfo;
import edu.pitt.dbmi.ccd.web.model.algo.FgsContinuousRunInfo;
import edu.pitt.dbmi.ccd.web.model.algo.FgsDiscreteRunInfo;
import edu.pitt.dbmi.ccd.web.prop.CcdProperties;
import edu.pitt.dbmi.ccd.web.service.algo.AlgorithmService;
import edu.pitt.dbmi.ccd.web.util.CmdOptions;
import java.util.Collections;
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
 * Nov 17, 2015 11:42:14 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "algorithm/fges")
public class FGESController implements ViewPath {

    private final AlgorithmService algorithmService;
    private final CcdProperties ccdProperties;

    @Autowired
    public FGESController(AlgorithmService algorithmService, CcdProperties ccdProperties) {
        this.algorithmService = algorithmService;
        this.ccdProperties = ccdProperties;
    }

    @RequestMapping(value = "disc", method = RequestMethod.GET)
    public String showFgesDiscreteView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        Map<String, String> dataset = algorithmService.getUserDiscreteDataset(appUser.getUsername());
        Map<String, String> prior = algorithmService.getUserPriorKnowledgeFiles(appUser.getUsername());
        FgsDiscreteRunInfo algoInfo = createDefaultFgsDiscreteRunInfo();

        // set the default dataset
        if (dataset.isEmpty()) {
            algoInfo.setDataset("");
        } else {
            algoInfo.setDataset(dataset.keySet().iterator().next());  // get one element
        }

        if (prior.isEmpty()) {
            algoInfo.setPriorKnowledge("");
        } else {
            algoInfo.setPriorKnowledge(prior.keySet().iterator().next());
        }

        model.addAttribute("datasetList", dataset);
        model.addAttribute("priorList", prior);
        model.addAttribute("algoInfo", algoInfo);

        return FGES_DISC_VIEW;
    }

    @RequestMapping(value = "disc", method = RequestMethod.POST)
    public String runFgesDiscrete(
            @ModelAttribute("algoInfo") final FgsDiscreteRunInfo algoInfo,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        AlgorithmJobRequest jobRequest = new AlgorithmJobRequest("FGESd", ccdProperties.getAlgoJar(), ccdProperties.getAlgoFgesDisc());
        jobRequest.setDataset(getDataset(algoInfo));
        jobRequest.setPriorKnowledge(getPriorKnowledge(algoInfo));
        jobRequest.setJvmOptions(getJvmOptions(algoInfo));
        jobRequest.setParameters(getParametersForDiscrete(algoInfo, appUser.getUsername()));

        algorithmService.addToQueue(jobRequest, appUser.getUsername());

        return REDIRECT_JOB_QUEUE;
    }

    @RequestMapping(value = "cont", method = RequestMethod.GET)
    public String showFgesContinuousView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        Map<String, String> dataset = algorithmService.getUserDataset(appUser.getUsername());
        Map<String, String> prior = algorithmService.getUserPriorKnowledgeFiles(appUser.getUsername());
        FgsContinuousRunInfo algoInfo = createDefaultFgsContinuousRunInfo();

        // set the default dataset
        if (dataset.isEmpty()) {
            algoInfo.setDataset("");
        } else {
            algoInfo.setDataset(dataset.keySet().iterator().next());  // get one element
        }

        if (prior.isEmpty()) {
            algoInfo.setPriorKnowledge("");
        } else {
            algoInfo.setPriorKnowledge(prior.keySet().iterator().next());
        }

        model.addAttribute("datasetList", dataset);
        model.addAttribute("priorList", prior);
        model.addAttribute("algoInfo", algoInfo);

        return FGES_CONT_VIEW;
    }

    @RequestMapping(value = "cont", method = RequestMethod.POST)
    public String runFgesContinuous(
            @ModelAttribute("algoInfo") final FgsContinuousRunInfo algoInfo,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        AlgorithmJobRequest jobRequest = new AlgorithmJobRequest("FGESc", ccdProperties.getAlgoJar(), ccdProperties.getAlgoFgesCont());
        jobRequest.setDataset(getDataset(algoInfo));
        jobRequest.setPriorKnowledge(getPriorKnowledge(algoInfo));
        jobRequest.setJvmOptions(getJvmOptions(algoInfo));
        jobRequest.setParameters(getParametersForContinuous(algoInfo, appUser.getUsername()));

        algorithmService.addToQueue(jobRequest, appUser.getUsername());

        return REDIRECT_JOB_QUEUE;
    }

    private List<String> getJvmOptions(AlgorithmRunInfo algoInfo) {
        List<String> jvmOptions = new LinkedList<>();

        int jvmMaxMem = algoInfo.getJvmMaxMem();
        if (jvmMaxMem > 0) {
            jvmOptions.add(String.format("-Xmx%dG", jvmMaxMem));
        }

        return jvmOptions;
    }

    private List<String> getDataset(AlgorithmRunInfo algoInfo) {
        return Collections.singletonList(algoInfo.getDataset());
    }

    private List<String> getPriorKnowledge(AlgorithmRunInfo algoInfo) {
        String priorKnowledge = algoInfo.getPriorKnowledge();
        if (priorKnowledge.trim().length() == 0) {
            return Collections.EMPTY_LIST;
        } else {
            return Collections.singletonList(algoInfo.getPriorKnowledge());
        }
    }

    private List<String> getParametersForDiscrete(FgsDiscreteRunInfo algoInfo, String username) {
        List<String> parameters = new LinkedList<>();
        String delimiter = algorithmService.getFileDelimiter(algoInfo.getDataset(), username);
        parameters.add(CmdOptions.DELIMITER);
        parameters.add(delimiter);
        parameters.add(CmdOptions.STRUCTURE_PRIOR);
        parameters.add(Double.toString(algoInfo.getStructurePrior()));
        parameters.add(CmdOptions.SAMPLE_PRIOR);
        parameters.add(Double.toString(algoInfo.getSamplePrior()));
        parameters.add(CmdOptions.MAX_DEGREE);
        parameters.add(Integer.toString(algoInfo.getMaxDegree()));
        if (algoInfo.isVerbose()) {
            parameters.add(CmdOptions.VERBOSE);
        }
        if (algoInfo.isFaithfulnessAssumed()) {
            parameters.add(CmdOptions.FAITHFULNESS_ASSUMED);
        }
        if (algoInfo.isSkipCategoryLimit()) {
            parameters.add(CmdOptions.SKIP_CATEGORY_LIMIT);
        }
        if (algoInfo.isSkipUniqueVarName()) {
            parameters.add(CmdOptions.SKIP_UNIQUE_VAR_NAME);
        }

        parameters.add(CmdOptions.TETRAD_GRAPH_JSON);

        return parameters;
    }

    private List<String> getParametersForContinuous(FgsContinuousRunInfo algoInfo, String username) {
        List<String> parameters = new LinkedList<>();
        String delimiter = algorithmService.getFileDelimiter(algoInfo.getDataset(), username);
        parameters.add(CmdOptions.DELIMITER);
        parameters.add(delimiter);
        parameters.add(CmdOptions.PENALTY_DISCOUNT);
        parameters.add(Double.toString(algoInfo.getPenaltyDiscount()));
        parameters.add(CmdOptions.MAX_DEGREE);
        parameters.add(Integer.toString(algoInfo.getMaxDegree()));
        if (algoInfo.isVerbose()) {
            parameters.add(CmdOptions.VERBOSE);
        }
        if (algoInfo.isFaithfulnessAssumed()) {
            parameters.add(CmdOptions.FAITHFULNESS_ASSUMED);
        }
        if (algoInfo.isSkipNonzeroVariance()) {
            parameters.add(CmdOptions.SKIP_NONZERO_VARIANCE);
        }
        if (algoInfo.isSkipUniqueVarName()) {
            parameters.add(CmdOptions.SKIP_UNIQUE_VAR_NAME);
        }

        parameters.add(CmdOptions.TETRAD_GRAPH_JSON);

        return parameters;
    }

    private FgsContinuousRunInfo createDefaultFgsContinuousRunInfo() {
        FgsContinuousRunInfo runInfo = new FgsContinuousRunInfo();
        runInfo.setPenaltyDiscount(CmdOptions.PENALTY_DISCOUNT_DEFAULT);
        runInfo.setMaxDegree(CmdOptions.MAX_DEGREE_DEFAULT);
        runInfo.setFaithfulnessAssumed(CmdOptions.FAITHFULNESS_ASSUMED_DEFAULT);
        runInfo.setSkipUniqueVarName(CmdOptions.SKIP_UNIQUE_VAR_NAME_DEFAULT);
        runInfo.setSkipNonzeroVariance(CmdOptions.SKIP_NONZERO_VARIANCE_DEFAULT);
        runInfo.setVerbose(CmdOptions.VERBOSE_DEFAULT);
        runInfo.setJvmMaxMem(1);

        return runInfo;
    }

    private FgsDiscreteRunInfo createDefaultFgsDiscreteRunInfo() {
        FgsDiscreteRunInfo runInfo = new FgsDiscreteRunInfo();
        runInfo.setSamplePrior(CmdOptions.SAMPLE_PRIOR_DEFAULT);
        runInfo.setStructurePrior(CmdOptions.STRUCTURE_PRIOR_DEFAULT);
        runInfo.setFaithfulnessAssumed(CmdOptions.FAITHFULNESS_ASSUMED_DEFAULT);
        runInfo.setMaxDegree(CmdOptions.MAX_DEGREE_DEFAULT);
        runInfo.setSkipUniqueVarName(CmdOptions.SKIP_UNIQUE_VAR_NAME_DEFAULT);
        runInfo.setSkipCategoryLimit(CmdOptions.SKIP_CATEGORY_LIMIT_DEFAULT);
        runInfo.setVerbose(CmdOptions.VERBOSE_DEFAULT);
        runInfo.setJvmMaxMem(1);

        return runInfo;
    }

}