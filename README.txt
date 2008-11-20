CmOpencmsShell uses quite a lot of original OpenCms source code which must be checked for changes
when updating to a new version. Code from the following places is used:
(7.0.3 to 7.0.5)                                                                           
VfsModule and VfsModuleOld:
	org.opencms.module.CmsModuleImportExportHandler (readModuleFromImport);	OK 
VfsOrgunits:
	org.opencms.db.generic.CmsUserDriver (createOrganizationalUnit, addResourceToOrganizationalUnit, internalCreateDefaultGroups);	OK
VfsSetup:
	CmsShell script WEB-INF/setupdata/cmssetup.txt from the original distribution	OK
VfsSync:
	org.opencms.synchronize.CmsSynchronize;		OK
	org.opencms.importexport.CmsImport; 		nicht ok - die Import/Export-Mechanismen wurden in OpenCms 7.0.4 komplett neu gestrickt
	org.opencms.importexport.CmsExport; 		nicht ok "
	org.opencms.importexport.CmsImportVersion4; nicht ok "
	org.opencms.importexport.CmsImportVersion5; nicht ok "
	org.opencms.importexport.A_CmsImport; 		nicht ok "
VfsUserExport:
	org.opencms.importexport.CmsExport; 		nicht ok "
VfsUserImport:
	org.opencms.importexport.CmsImportVersion4; nicht ok "
	org.opencms.importexport.CmsImportVersion5; nicht ok "
	org.opencms.importexport.CmsImportVersion6; nicht ok "
	org.opencms.importexport.A_CmsImport; 		nicht ok "
XmlHandling:
	org.opencms.importexport.CmsExport; 		nicht ok "
CmOpencmsShell:
	org.opencms.main.CmsShell; 					OK
	org.opencms.main.CmsShellCommands (login); 	OK
CmsSynchronize:
	org.opencms.synchronize.CmsSynchronize; 	OK
