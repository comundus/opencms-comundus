CmOpencmsShell uses quite a lot of original OpenCms source code which must be checked for changes
when updating to a new version. Code from the following places is used:
(7.0.1 to 7.0.3)                                                                           
VfsModule and VfsModuleOld:
	org.opencms.module.CmsModuleImportExportHandler (readModuleFromImport); OK
VfsOrgunits:
	org.opencms.db.generic.CmsUserDriver (createOrganizationalUnit, addResourceToOrganizationalUnit); OK
VfsSetup:
	CmsShell script WEB-INF/setupdata/cmssetup.txt from the original distribution
VfsSync:
	org.opencms.synchronize.CmsSynchronize; OK
	org.opencms.importexport.CmsImport; OK
	org.opencms.importexport.CmsExport; OK
	org.opencms.importexport.CmsImportVersion4; OK
	org.opencms.importexport.CmsImportVersion5; OK
	org.opencms.importexport.A_CmsImport; OK
VfsUserExport:
	org.opencms.importexport.CmsExport; OK
VfsUserImport:
	org.opencms.importexport.CmsImportVersion4; OK
	org.opencms.importexport.CmsImportVersion5; OK
	org.opencms.importexport.CmsImportVersion6; OK
	org.opencms.importexport.A_CmsImport; OK
XmlHandling:
	org.opencms.importexport.CmsExport; OK
CmOpencmsShell:
	org.opencms.main.CmsShell; OK
	org.opencms.main.CmsShellCommands; OK
CmsSynchronize:
	org.opencms.synchronize.CmsSynchronize; OK
