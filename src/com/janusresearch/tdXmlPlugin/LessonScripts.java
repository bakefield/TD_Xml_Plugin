package com.janusresearch.tdXmlPlugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.DomManager;
import com.janusresearch.tdXmlPlugin.debug.Debug;
import com.janusresearch.tdXmlPlugin.dialog.LessonScriptsDialog;
import com.janusresearch.tdXmlPlugin.dom.COL;
import com.janusresearch.tdXmlPlugin.dom.COLs;
import com.janusresearch.tdXmlPlugin.dom.Module;
import com.janusresearch.tdXmlPlugin.xml.FrameSet;
import com.janusresearch.tdXmlPlugin.xml.ScriptGenerator;
import com.janusresearch.tdXmlPlugin.xml.StepTree;

import java.util.ArrayList;
import java.util.List;

public class LessonScripts extends AnAction {
    @Override
    @SuppressWarnings("ConstantConditions")
    public void actionPerformed(AnActionEvent e) {
        //Get Project from the data from data keys
        Project project = e.getData(PlatformDataKeys.PROJECT);

        //Create and display the Lesson Scripts dialog
        LessonScriptsDialog dialog = new LessonScriptsDialog();
        dialog.show();

        if (dialog.isOK()) {
            FileChooserDescriptor descriptor = null;
            //Set File Choice criteria and File Browser information
            if (LessonScriptsDialog.isSpecificLessons()) {
                descriptor = new FileChooserDescriptor(true, false, false, false, false, true);
                descriptor.setTitle("Select One or More Lessons");
                descriptor.setDescription("Select one or more lessons from a Project's Lessons folder for which you want to create Audio Scripts.  Multiple files can be selected using the Shift or Ctrl key.  Component (Zero i.e. BX0A0101), Popup (POP), and Preload (PL) files are ignored even if selected.");
            } else if (LessonScriptsDialog.isProjectLessons()) {//Set File Choice criteria and open the file browser
                descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
                descriptor.setTitle("Select a Project's Lessons Folder");
                descriptor.setDescription("Select a Project's Lessons folder for which you want to create Audio Scripts.  Component (Zero i.e. BX0A0101), Popup (POP), and Preload (PL) files are ignored even if selected.");
            }
            final boolean[] isLessonFile = new boolean[1];
            final boolean[] isColFile = new boolean[1];

            //Open File/Path chooser and gather the Lesson XML files from the selections made
            FileChooser.chooseFiles(descriptor, project, null, files -> {
                final ProgressManager pm = ProgressManager.getInstance();
                List<VirtualFile> xmlFiles = new ArrayList<>();
                final boolean hasSelection = pm.runProcessWithProgressSynchronously((Runnable) () -> xmlFiles.addAll(files), "Looking for Lesson Files...", false, project);
                if (!hasSelection || xmlFiles.isEmpty()) return;

                if (xmlFiles.size() == 1 && xmlFiles.get(0).isDirectory()) {
                    VirtualFile vFile = xmlFiles.get(0);
                    xmlFiles.clear();
                    for (VirtualFile v : vFile.getChildren()) {
                        isLessonFile[0] = v.getName().matches("[A-Z]{2}1[A-Z][0-9]{4}[a-zA-Z]?\\.xml");
                        isColFile[0] = v.getName().matches("[A-Z]{2}1[A-Z][0-9]{4}[a-zA-Z]?(?:COL)\\.xml");
                        if (isLessonFile[0]) {
                            xmlFiles.add(v);
                        }
                    }
                }

                //Create DomManager, FileDescription and register the description
                DomManager manager = DomManager.getDomManager(project);

                //Create a Script Generator object and store the acronym pronunciations in memory
                ScriptGenerator sg = new ScriptGenerator(project);
                sg.createAcronymPronunciations(manager, pm);

                pm.runProcessWithProgressSynchronously(() -> {
                    if (xmlFiles.size() != 0) {
                        for (VirtualFile v : xmlFiles) {
                            //get the Virtual file as an XML File and get the name of the file
                            XmlFile xmlFile = (XmlFile) PsiManager.getInstance(project).findFile(v);
                            String fileName = xmlFile.getName().replaceFirst("\\..*", "");

                            if (xmlFile != null) {
                                if (isLessonFile[0]) {
                                    Module moduleRoot = manager.getFileElement(xmlFile, Module.class).getRootElement();

                                    //Create StepTree and FrameSet objects to collect lesson data and then use that data
                                    //to generate the audio scripts for the selected lesson
                                    StepTree stepTree = new StepTree(moduleRoot);
                                    FrameSet frameSet = new FrameSet(project, moduleRoot);
                                    stepTree.storeNodes();
                                    frameSet.storeFrames();
                                    sg.createLessonScript(fileName, stepTree, frameSet);
                                } else if (isColFile[0]) {
                                    COLs colsRoot = manager.getFileElement(xmlFile, COLs.class).getRootElement();
                                    List<COL> cols = colsRoot.getCOLs();
                                    COL col = cols.get(3);
                                    String rawText = col.getType().getRawText();
                                    Debug.print(rawText);
                                    Debug.print(col.getQuestions().getQuestion1().getRawText());
                                    WriteCommandAction.runWriteCommandAction(project, () -> {
                                        col.getQuestions().getQuestion1().setValue("this is a test does this work");
                                    });
                                }
                            }
                        }
                    }
                }, "Generating Scripts", false, project);
            });
        }
    }

    @Override
    public void update(AnActionEvent e) {
        //Set visibility of plugin button
        e.getPresentation().setEnabled(true);
    }
}
