/*
 * Copyright (C) 2017 University of Pittsburgh.
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
package edu.pitt.dbmi.ccd.web.ctrl.file;

import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileFormatService;
import edu.pitt.dbmi.ccd.db.service.FileGroupService;
import edu.pitt.dbmi.ccd.db.service.FileService;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.domain.file.FileGroupForm;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
import edu.pitt.dbmi.ccd.web.service.file.FileGroupingService;
import java.util.List;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * Jun 29, 2017 10:57:02 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "secured/file/group")
public class FileGroupController implements ViewPath {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileGroupController.class);

    private final FileService fileService;
    private final FileGroupService fileGroupService;
    private final FileGroupingService fileGroupingService;
    private final AppUserService appUserService;

    @Autowired
    public FileGroupController(FileService fileService, FileGroupService fileGroupService, FileGroupingService fileGroupingService, AppUserService appUserService) {
        this.fileService = fileService;
        this.fileGroupService = fileGroupService;
        this.fileGroupingService = fileGroupingService;
        this.appUserService = appUserService;
    }

    @ResponseBody
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public ResponseEntity<?> deleteFile(
            @RequestParam(value = "id") final Long id,
            final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        if (fileGroupService.getRepository().existsByIdAndUserAccount(id, userAccount)) {
            try {
                fileGroupService.getRepository().deleteByIdAndUserAccount(id, userAccount);
            } catch (Exception exception) {
                LOGGER.error(exception.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to delete file group.");
            }

            return ResponseEntity.ok(id);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "new", method = RequestMethod.POST)
    public String createFileGroup(
            @Valid @ModelAttribute("fileGroupForm") final FileGroupForm fileGroupForm,
            final BindingResult bindingResult,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model,
            final RedirectAttributes redirAttrs) {
        if (bindingResult.hasErrors()) {
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.fileGroupForm", bindingResult);
            redirAttrs.addFlashAttribute("fileGroupForm", fileGroupForm);

            return REDIRECT_FILEGROUP_VIEW;
        }

        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        if (fileGroupService.getRepository().existsByNameAndUserAccount(fileGroupForm.getGroupName(), userAccount)) {
            bindingResult.rejectValue("groupName", "fileGroupForm.groupName", "Name already existed.");
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.fileGroupForm", bindingResult);
            redirAttrs.addFlashAttribute("fileGroupForm", fileGroupForm);

            return REDIRECT_FILEGROUP_VIEW;
        } else {
            fileGroupingService.addFileGroup(fileGroupForm, userAccount);
        }

        return REDIRECT_FILEGROUP_LIST;
    }

    @RequestMapping(value = "new", method = RequestMethod.GET)
    public String showFileGroup(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        List<File> files = fileService.getRepository().findByUserAccountAndFileFormatName(userAccount, FileFormatService.TETRAD_TABULAR);

        if (!model.containsAttribute("fileGroupForm")) {
            model.addAttribute("fileGroupForm", new FileGroupForm());
        }
        model.addAttribute("files", files);

        return FILEGROUP_VIEW;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showFileGroupList(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        return FILEGROUP_LIST_VIEW;
    }

    @ResponseBody
    @RequestMapping(value = "list", method = RequestMethod.GET)
    public ResponseEntity<?> listFileGrops(
            final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        if (userAccount == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(fileGroupingService.getFileGroups(userAccount));
    }

}