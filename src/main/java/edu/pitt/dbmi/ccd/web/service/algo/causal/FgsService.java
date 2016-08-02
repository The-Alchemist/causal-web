/*
 * Copyright (C) 2016 University of Pittsburgh.
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
package edu.pitt.dbmi.ccd.web.service.algo.causal;

import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.FileType;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileService;
import edu.pitt.dbmi.ccd.db.service.FileTypeService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.domain.algo.FgscRunInfo;
import edu.pitt.dbmi.ccd.web.exception.ResourceNotFoundException;
import edu.pitt.dbmi.ccd.web.service.file.FileManagementService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

/**
 *
 * Jul 29, 2016 12:34:59 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class FgsService {

    private final UserAccountService userAccountService;
    private final FileService fileService;
    private final FileManagementService fileManagementService;

    @Autowired
    public FgsService(UserAccountService userAccountService, FileService fileService, FileManagementService fileManagementService) {
        this.userAccountService = userAccountService;
        this.fileService = fileService;
        this.fileManagementService = fileManagementService;
    }

    public void showFgsContinuousView(AppUser appUser, Model model) {
        UserAccount userAccount = userAccountService.findByUsername(appUser.getUsername());
        if (userAccount == null) {
            throw new ResourceNotFoundException();
        }

        FileType fileType = fileManagementService.getFileType(FileTypeService.DATA_TYPE_NAME);
        List<File> datasetList = fileService.findByFileTypeAndUserAccount(fileType, userAccount);

        fileType = fileManagementService.getFileType(FileTypeService.VAR_TYPE_NAME);
        List<File> varList = fileService.findByFileTypeAndUserAccount(fileType, userAccount);

        fileType = fileManagementService.getFileType(FileTypeService.PRIOR_TYPE_NAME);
        List<File> priorList = fileService.findByFileTypeAndUserAccount(fileType, userAccount);

        FgscRunInfo fgscRunInfo = new FgscRunInfo();
        fgscRunInfo.setVarFileId(0L);
        fgscRunInfo.setPriorFileId(0L);
        // set the default dataset
        if (!datasetList.isEmpty()) {
            fgscRunInfo.setDataFileId(datasetList.get(0).getId());  // get one element
        }

        model.addAttribute("datasetList", datasetList);
        model.addAttribute("varList", varList);
        model.addAttribute("priorList", priorList);
        model.addAttribute("fgscRunInfo", fgscRunInfo);
    }

}