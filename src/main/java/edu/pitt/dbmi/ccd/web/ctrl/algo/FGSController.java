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
import static edu.pitt.dbmi.ccd.web.ctrl.ViewPath.REDIRECT_JOB_QUEUE;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.model.algo.FgsRunInfo;
import edu.pitt.dbmi.ccd.web.service.algo.AlgorithmService;
import edu.pitt.dbmi.ccd.web.service.cloud.dto.AlgorithmJobRequest;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 * Fast Greedy Search (FGS) Controller.
 *
 * Nov 10, 2015 3:13:04 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "/algorithm/fgs")
public class FGSController implements ViewPath {

    private static final Logger LOGGER = LoggerFactory.getLogger(FGSController.class);

    private final String fgs;

    protected final String algorithmJar;

    private final AlgorithmService algorithmService;

    @Autowired(required = true)
    public FGSController(
            @Value("${ccd.algorithm.fgs}") String fgs,
            @Value("${ccd.algorithm.jar}") String algorithmJar,
            AlgorithmService algorithmService) {
        this.fgs = fgs;
        this.algorithmJar = algorithmJar;
        this.algorithmService = algorithmService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showGesView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        FgsRunInfo info = new FgsRunInfo();
        info.setPenaltyDiscount(4.0);
        info.setDepth(3);
        info.setVerbose(Boolean.TRUE);
        info.setJvmOptions("");
        info.setRunOnPsc(Boolean.FALSE);

        Map<String, String> map = algorithmService.getUserRunnableData(appUser.getUsername());
        if (map.isEmpty()) {
            info.setDataset("");
        } else {
            Set<String> keySet = map.keySet();
            for (String key : keySet) {
                info.setDataset(key);
                break;
            }
        }

        model.addAttribute("datasetList", map);
        model.addAttribute("algoInfo", info);

        return FGS_VIEW;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String runGes(
            @ModelAttribute("algoInfo") final FgsRunInfo info,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        List<String> params = new LinkedList<>();

        String delimiter = algorithmService.getFileDelimiter(appUser.getDataDirectory(), info.getDataset());
        params.add("--delimiter");
        params.add(delimiter);

        params.add("--penalty-discount");
        params.add(String.valueOf(info.getPenaltyDiscount().doubleValue()));

        params.add("--depth");
        params.add(String.valueOf(info.getDepth().intValue()));

        if (info.getVerbose()) {
            params.add("--verbose");
        }

        String[] jvmOptions = null;
        String jvmOpts = info.getJvmOptions().trim();
        if (jvmOpts.length() > 0) {
            jvmOptions = jvmOpts.split("\\s+");
        }

        AlgorithmJobRequest jobRequest = new AlgorithmJobRequest();
        jobRequest.setAlgorithmName("fgs");
        jobRequest.setAlgorithm(fgs);
        jobRequest.setJvmOptions(jvmOptions);
        jobRequest.setParameters(params.toArray(new String[params.size()]));
        jobRequest.setDataset(info.getDataset());

        if (info.getRunOnPsc()) {
            algorithmService.runRemotely(jobRequest, appUser);
        } else {
            algorithmService.runLocally(algorithmJar, jobRequest, appUser);
        }

        return REDIRECT_JOB_QUEUE;
    }

}