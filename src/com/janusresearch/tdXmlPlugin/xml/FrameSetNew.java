package com.janusresearch.tdXmlPlugin.xml;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.janusresearch.tdXmlPlugin.dialog.SubStepsDialog;
import com.janusresearch.tdXmlPlugin.dom.Module;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("ConstantConditions")
public class FrameSetNew {
    private Module root;
    private XmlTag[] frames;
    private int frameCount;
    private Project myProject;
    private XmlAttribute[][] frameAttributes;
    private String[][] oldFrameValues;
    private String[][] newFrameValues;
    private List<XmlAttribute> playEvents = new ArrayList<>();
    private List<XmlAttribute> backEvents = new ArrayList<>();
    private List<XmlAttribute> otherEvents = new ArrayList<>();
    private List<String> newPlayValues = new ArrayList<>();
    private List<String> newBackValues = new ArrayList<>();
    private List<String> newOtherValues = new ArrayList<>();
    private int actualStepCount;

    public FrameSetNew(Project project, Module moduleRoot) {
        myProject = project;
        root = moduleRoot;
    }

    /** Store every Frame sub tag from Frame Set in the frames array */
    public void storeFrames() {
        frames = root.getXmlTag().findFirstSubTag("FrameSet").findSubTags("Frame");
        setFrameCount(frames);
    }

    /** Store the length of the FrameSet as frameCount */
    private void setFrameCount(XmlTag[] frames) {
        frameCount = frames.length;
    }

    /** Store the reference to all Frame attributes from FrameSet in the frameAttributes array */
    public void storeFrameAttributes() {
        storeFrames();
        frameAttributes = new XmlAttribute[frameCount][3];
        int i = 0;
        for (XmlTag x : getFrames()) {
            frameAttributes[i][0] = x.getAttribute("id");
            frameAttributes[i][1] = x.getAttribute("node");
            frameAttributes[i][2] = x.getAttribute("weight");
            i++;
        }
    }

    public boolean isPlayFrame(XmlTag x) {
        boolean playFrame = false;
        for (XmlTag e : getFrameEvents(x)) {
            String get = e.getAttribute("get").getValue();
            if (Objects.equals(get, "Play")) {
                playFrame = true;
                break;
            }
            else {
                playFrame = false;
            }
        }
        return playFrame;
    }

    /** Store the old values for each Frame in the oldFrameValues array */
    public void storeOldFrameValues() {
        oldFrameValues = new String[frameCount][3];
        int i = 0;
        for (XmlAttribute[] x : getFrameAttributes()) {
            oldFrameValues[i][0] = x[0].getValue();
            oldFrameValues[i][1] = x[1].getValue();
            oldFrameValues[i][2] = x[2].getValue();
            i++;
        }
    }

    /** Store the new values for each Frame in the newFrameValues array */
    public void storeNewFrameValues() {
        //Set array size based on nodes array length
        newFrameValues = new String[frameCount][3];
        int i = 0;
        String oldLastNode;
        String oldCurrentNode;
        String newLastNode;
        for (String[] s : getNewFrameValues()) {
            //store the new values for each frame
            if (i == 0) {
                s[0] = "01";
                s[1] = "01";
                s[2] = "01";
            }
            else {
                oldLastNode = getFrameAttributes()[i - 1][1].getValue();
                oldCurrentNode = getFrameAttributes()[i][1].getValue();

                if (i < 9) {
                    s[0] = "0" + (i + 1);
                }
                else {
                    s[0] = String.valueOf(i + 1);
                }

                newLastNode = getNewFrameValues()[i - 1][1];
                if (Objects.equals(oldCurrentNode, oldLastNode) && !Objects.equals(oldCurrentNode, "") && SubStepsDialog.hasSubSteps()) {
                    s[1] = newLastNode;
                    s[2] = newLastNode;
                }
                else {
                    int n = Integer.parseInt(newLastNode) + 1;
                    if (n < 10) {
                        s[1] = "0" + String.valueOf(n);
                        s[2] = "0" + String.valueOf(n);
                    }
                    else {
                        s[1] = String.valueOf(n);
                        s[2] = String.valueOf(n);
                    }
                }
            }
            i++;
        }
    }

    /** Get all Event sub tags from a Frame */
    @NotNull
    public XmlTag[] getFrameEvents(XmlTag x) {
        return x.findFirstSubTag("Events").findSubTags("Event");
    }

    /** Get the Text value from the Frame */
    @NotNull
    public String getFrameInfoText(XmlTag x) {
        String text = "";
        if (x.findFirstSubTag("InfoText") != null) {
            text = x.findFirstSubTag("InfoText").getValue().getText();
        }
        return text;
    }


    /** Get the Text value from the Frame */
    @NotNull
    public String getFrameText(XmlTag x) {
        String text = "";
        if (x.findFirstSubTag("Text") != null) {
            text = x.findFirstSubTag("Text").getValue().getText();
        }
        return text;
    }

    /** Get the Text2 value from the Frame */
    @NotNull
    public String getFrameText2(XmlTag x) {
        String text = "";
        if (x.findFirstSubTag("Text2") != null) {
            text = x.findFirstSubTag("Text2").getValue().getText();
        }
        return text;
    }

    /** Returns boolean true if the Frame has a steps attribute otherwise false */
    private boolean hasSteps(XmlTag x) {
        return x.getAttribute("steps") != null;
    }

    /** Returns boolean true if the Frame has a steps attribute with no value otherwise false */
    private boolean isStepsEmpty(XmlTag x) {
        return x.getAttribute("steps").getValue().isEmpty();
    }

    /** Returns boolean true if the Frame has a steps attribute with a value of zero otherwise false */
    private boolean isStepsZero(XmlTag x) {
        return x.getAttribute("steps").getValue().equals("0");
    }

    /** Returns the step count from a lesson. If the lesson contains step attributes in each Frame it will use
     * those values for the count.  If they do not have the steps attribute or if the steps attribute is an empty
     * String or equals zero then attribute is added/updated with a value that is an approximate value
     * determined through processing the Text2 block
     */
    public int getStepCount() {
        storeFrames();
        actualStepCount = 0;
        WriteCommandAction.runWriteCommandAction(myProject, () -> {
            for (XmlTag f : getFrames()) {
                if (!hasSteps(f) || (hasSteps(f) && (isStepsEmpty(f) || isStepsZero(f)))) {
                    int stepCount = parseTextForCount(getFrameText2(f));
                    f.setAttribute("steps", String.valueOf(stepCount));
                }
                actualStepCount += Integer.parseInt(f.getAttribute("steps").getValue());
            }
        });
        return actualStepCount;
    }

    /** Parses the Text2 text value to attempt to determine the number of steps in a given Frame */
    private int parseTextForCount(String s) {
        int count = 1;

        //parse String s to determine an approximate count for the Frame
        //for Play Frames set steps="1"

        return count;
    }

    /** Process Events from every Frame to determine new nextid values */
    public void processEvents() {
        int i = 0;
        for (XmlTag f : getFrames()) {
            for (XmlTag e : getFrameEvents(f)) {
                XmlAttribute nextid = e.getAttribute("nextid");
                if (nextid != null) {
                    String get = e.getAttribute("get").getValue();
                    if (Objects.equals(get, "Play")) {
                        addPlayEvent(nextid);
                        addNewPlayValue(i);
                    }
                    else if (Objects.equals(get, "Back")) {
                        addBackEvent(nextid);
                        addNewBackValue(i);
                    }
                    else {
                        addOtherEvent(nextid);
                        addNewOtherValue(i);
                    }
                }
            }
            i++;
        }
    }

    /** Adds reference to Play nextid attribute to the list*/
    private void addPlayEvent(XmlAttribute x) {
        playEvents.add(x);
    }

    /** Adds the Play nextid new value to the list */
    private void addNewPlayValue(int i) {
        if (i < 8) {
            getNewPlayValues().add("0" + (i + 2));
        }
        else {
            getNewPlayValues().add(String.valueOf(i + 2));
        }
    }

    /** Adds reference to Back nextid attribute to the list */
    private void addBackEvent(XmlAttribute x) {
        backEvents.add(x);
    }

    /** Adds the Back nextid new value to the list */
    private void addNewBackValue(int i) {
        if (i < 10) {
            getNewBackValues().add("0" + i);
        }
        else {
            getNewBackValues().add(String.valueOf(i));
        }
    }

    /** Adds reference to Other nextid attribute to the list */
    private void addOtherEvent(XmlAttribute x) {
        otherEvents.add(x);
    }

    /** Adds the Other nextid new value to the list */
    private void addNewOtherValue(int i) {
        if (i < 8) {
            getNewOtherValues().add("0" + (i + 2));
        }
        else {
            getNewOtherValues().add(String.valueOf(i + 2));
        }
    }

    /** Returns the newPlayValues list */
    public List<String> getNewPlayValues() {
        return newPlayValues;
    }

    /** Returns the newBackValues list */
    public List<String> getNewBackValues() {
        return newBackValues;
    }

    /** Returns the newOtherValues list */
    public List<String> getNewOtherValues() {
        return newOtherValues;
    }

    /** Returns the playEvents list */
    public List<XmlAttribute> getPlayEvents() {
        return playEvents;
    }

    /** Returns the backEvents list */
    public List<XmlAttribute> getBackEvents() {
        return backEvents;
    }

    /** Returns the otherEvents list */
    public List<XmlAttribute> getOtherEvents() {
        return otherEvents;
    }

    /** Returns the frames array */
    @Contract(pure = true)
    public XmlTag[] getFrames() {
        return frames;
    }

    /** Returns the nodeCount */
    public int getFrameCount() {
        return frameCount;
    }

    /** Returns the nodeAttributes array */
    public XmlAttribute[][] getFrameAttributes() {
        return frameAttributes;
    }

    /** Returns the oldNodeValues array */
    public String[][] getOldFrameValues() {
        return oldFrameValues;
    }

    /** Returns the newNodeValues array */
    public String[][] getNewFrameValues() {
        return newFrameValues;
    }
}