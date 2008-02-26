/**
 * $Revision$
 * $Date$
 *
 * Copyright (C) 2006 Jive Software. All rights reserved.
 *
 * This software is published under the terms of the GNU Public License (GPL),
 * a copy of which is included in this distribution.
 */
package org.jivesoftware.openfire.clearspace;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.user.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author Gabriel Guardincerri
 */
class ClearspaceVCardTranslator {

    // Represents the type of action that was performed.
    enum Action {
        MODIFY, CREATE, UPDATE, DELETE, NO_ACTION
    }

    /**
     * Represents the default fields of Clearspace, this is only a subset of the fields. It only includes
     * the fields that have in common with VCards.
     */
    enum ClearspaceField {

        TITLE("Title"),
        DEPARTMENT("Department"),
        TIME_ZONE("Time Zone"),
        ADDRESS("Address"),
        HOME_ADDRESS("Home Address"),
        ALT_EMAIL("Alternate Email", true), //multiple - primary comes from user obj
        URL("URL", true), //multiple with primary
        PHONE("Phone Number", true); //multiple with primary

        // Used to get a field from its ID
        private static Map<Long, ClearspaceField> idMap = new HashMap<Long, ClearspaceField>();

        // Used to get a field from its name
        private static Map<String, ClearspaceField> nameMap = new HashMap<String, ClearspaceField>();

        static {
            nameMap.put(TITLE.getName(), TITLE);
            nameMap.put(DEPARTMENT.getName(), DEPARTMENT);
            nameMap.put(TIME_ZONE.getName(), TIME_ZONE);
            nameMap.put(ADDRESS.getName(), ADDRESS);
            nameMap.put(HOME_ADDRESS.getName(), HOME_ADDRESS);
            nameMap.put(ALT_EMAIL.getName(), ALT_EMAIL);
            nameMap.put(URL.getName(), URL);
            nameMap.put(PHONE.getName(), PHONE);
        }

        // The name is fixed and can be used as ID
        private final String name;
        // The id may change, so it is updated
        private long id;
        // True if the field supports multiple values.
        private final boolean multipleValues;

        /**
         * Constructs a new field with a name
         *
         * @param name the name of the field
         */
        ClearspaceField(String name) {
            this(name, false);
        }

        /**
         * Constructs a new field with a name and if it has multiple values
         *
         * @param name           the name of the field
         * @param multipleValues true if it has multiple values
         */
        ClearspaceField(String name, boolean multipleValues) {
            this.name = name;
            this.multipleValues = multipleValues;
        }

        public String getName() {
            return name;
        }

        public long getId() {
            return id;
        }

        public boolean isMultipleValues() {
            return multipleValues;
        }

        public void setId(long id) {
            this.id = id;
            idMap.put(id, this);
        }

        public static ClearspaceField valueOf(long id) {
            return idMap.get(id);
        }

        public static ClearspaceField valueOfName(String name) {
            return nameMap.get(name);
        }

    }

    /**
     * Represents the fields of the VCard, this is only a subset of the fields. It only includes
     * the fields that have in common with Clearspace.
     */
    enum VCardField {
        TITLE, ORG_ORGUNIT, ADR_WORK, ADR_HOME, EMAIL_USERID, EMAIL_PREF_USERID, FN,
        PHOTO_TYPE, PHOTO_BINVAL, URL, TZ, PHONE_HOME, PHONE_WORK, FAX_WORK, MOBILE_WORK, PAGER_WORK
    }

    private static ClearspaceVCardTranslator instance = new ClearspaceVCardTranslator();

    /**
     * Returns the instance of the translator
     *
     * @return the instance.
     */
    protected static ClearspaceVCardTranslator getInstance() {
        return instance;
    }

    /**
     * Init the fields of clearspace based on they name.
     *
     * @param fieldsId
     */
    protected void initClearspaceFieldsId(Element fieldsId) {
        List<Element> fieldsList = fieldsId.elements("return");
        for (Element field : fieldsList) {
            String fieldName = field.elementText("name");
            long fieldID = Long.valueOf(field.elementText("ID"));

            ClearspaceField f = ClearspaceField.valueOfName(fieldName);
            if (f != null) {
                f.setId(fieldID);
            }
        }

    }

    protected Action[] translateVCard(Element vCardElement, Element profilesElement, Element userElement, Element avatarElement) {
        Action[] actions = new Action[3];

        // Gets the vCard values
        Map<VCardField, String> vCardValues = collectVCardValues(vCardElement);

        // Updates the profiles values with the vCard values
        actions[0] = updateProfilesValues(profilesElement, vCardValues);

        actions[1] = updateUserValues(userElement, vCardValues);

        actions[2] = updateAvatarValues(avatarElement, vCardValues);

        return actions;
    }

    private Action updateAvatarValues(Element avatarElement, Map<VCardField, String> vCardValues) {
        Action action = Action.NO_ACTION;

        // Gets the current avatar information
        String currContentType = avatarElement.elementText("contentType");
        String currdata = avatarElement.elementText("data");

        // Gets the vCard photo information
        String newContentType = vCardValues.get(VCardField.PHOTO_TYPE);
        String newData = vCardValues.get(VCardField.PHOTO_BINVAL);

        // Compares them
        if (currContentType == null && newContentType != null) {
            // new avatar
            avatarElement.addElement("contentType").setText(newContentType);
            avatarElement.addElement("data").setText(newData);
            action = Action.CREATE;
        } else if (currContentType != null && newContentType == null) {
            // delete
            action = Action.DELETE;
        } else if (currdata != null && !currdata.equals(newData)) {
            // modify
            avatarElement.element("contentType").setText(newContentType);
            avatarElement.element("data").setText(newData);
            action = Action.MODIFY;
        }

        return action;
    }

    private Action updateUserValues(Element userElement, Map<VCardField, String> vCardValues) {
        Action action = Action.NO_ACTION;

        String fullName = vCardValues.get(VCardField.FN);

        boolean emptyName = fullName != null && !"".equals(fullName.trim());

        // if the new value is not empty then update. The name can't be deleted by an empty string
        if (!emptyName) {
            WSUtils.modifyElementText(userElement, "name", fullName);
            action = Action.MODIFY;
        }

        String email = vCardValues.get(VCardField.EMAIL_PREF_USERID);

        boolean emptyEmail = email != null && !"".equals(email.trim());
        // if the new value is not empty then update. The email can't be deleted by an empty string
        if (!emptyEmail) {
            WSUtils.modifyElementText(userElement, "email", email);
            action = Action.MODIFY;
        }
        return action;
    }

    /**
     * Updates the values of the profiles with the values of the vCard
     *
     * @param profiles
     * @param vCardValues
     * @return
     */
    private Action updateProfilesValues(Element profiles, Map<VCardField, String> vCardValues) {
        Action action = Action.NO_ACTION;

        List<Element> profilesList = profiles.elements("profiles");

        // Modify or delete current profiles
        for (Element profile : profilesList) {
            int fieldID = Integer.valueOf(profile.elementText("fieldID"));
            ClearspaceField field = ClearspaceField.valueOf(fieldID);

            // If the field is unknown, then continue with the next one
            if (field == null) {
                continue;
            }

            // Gets the field value, it could have "value" of "values"
            Element value = profile.element("value");
            if (value == null) {
                value = profile.element("values");
                // It's OK if the value still null. It could need to be modified
            }

            // Modify or delete each field type. If newValue is null it will empty the field.
            String newValue;
            String oldValue;
            switch (field) {
                case TITLE:
                    if (modifyProfileValue(vCardValues, profiles, value, VCardField.TITLE)) {
                        action = Action.MODIFY;
                    }
                    break;
                case DEPARTMENT:
                    if (modifyProfileValue(vCardValues, profiles, value, VCardField.ORG_ORGUNIT)) {
                        action = Action.MODIFY;
                    }
                    break;
                case ADDRESS:
                    if (modifyProfileValue(vCardValues, profiles, value, VCardField.ADR_WORK)) {
                        action = Action.MODIFY;
                    }
                    break;
                case HOME_ADDRESS:
                    if (modifyProfileValue(vCardValues, profiles, value, VCardField.ADR_HOME)) {
                        action = Action.MODIFY;
                    }
                    break;
                case TIME_ZONE:
                    if (modifyProfileValue(vCardValues, profiles, value, VCardField.TZ)) {
                        action = Action.MODIFY;
                    }
                    break;
                case URL:
                    if (modifyProfileValue(vCardValues, profiles, value, VCardField.URL)) {
                        action = Action.MODIFY;
                    }
                    break;
                case ALT_EMAIL:
                    // Get the new value
                    newValue = vCardValues.get(VCardField.EMAIL_USERID);
                    // Get the old value
                    oldValue = value.getTextTrim();
                    // Get the mail type, i.e. HOME or WORK
                    String mailType = getFieldType(oldValue);
                    // Adds the mail type to the new value
                    newValue = addFieldType(newValue, mailType);
                    // Now the old and new values can be compared
                    if (!oldValue.equalsIgnoreCase(newValue)) {
                        value.setText(newValue == null ? "" : newValue);
                        action = Action.MODIFY;
                    }

                    // Removes the value from the map to mark that is was used
                    vCardValues.remove(VCardField.EMAIL_USERID);
                    break;
                case PHONE:
                    // Get all the phones numbers
                    String newHomePhone = vCardValues.get(VCardField.PHONE_HOME);
                    String newWorkPhone = vCardValues.get(VCardField.PHONE_WORK);
                    String newWorkFax = vCardValues.get(VCardField.FAX_WORK);
                    String newWorkMobile = vCardValues.get(VCardField.MOBILE_WORK);
                    String newWorkPager = vCardValues.get(VCardField.PAGER_WORK);
                    newValue = null;


                    oldValue = value.getTextTrim();
                    String oldType = getFieldType(oldValue);

                    // Modifies the phone field that is of the same type
                    if ("work".equalsIgnoreCase(oldType)) {
                        newValue = addFieldType(newWorkPhone, oldType);

                    } else if ("home".equalsIgnoreCase(oldType)) {
                        newValue = addFieldType(newHomePhone, oldType);

                    } else if ("fax".equalsIgnoreCase(oldType)) {
                        newValue = addFieldType(newWorkFax, oldType);

                    } else if ("mobile".equalsIgnoreCase(oldType)) {
                        newValue = addFieldType(newWorkMobile, oldType);

                    } else if ("pager".equalsIgnoreCase(oldType)) {
                        newValue = addFieldType(newWorkPager, oldType);

                    } else if ("other".equalsIgnoreCase(oldType)) {
                        // No phone to update
                        // Removes the values from the map to mark that is was used
                        vCardValues.remove(VCardField.PHONE_HOME);
                        vCardValues.remove(VCardField.PHONE_WORK);
                        vCardValues.remove(VCardField.FAX_WORK);
                        vCardValues.remove(VCardField.MOBILE_WORK);
                        vCardValues.remove(VCardField.PAGER_WORK);
                        break;
                    }

                    // If newValue and oldValue are different the update the field
                    if (!oldValue.equals(newValue)) {
                        value.setText(newValue == null ? "" : newValue);
                        action = Action.MODIFY;
                    }

                    // Removes the values from the map to mark that is was used
                    vCardValues.remove(VCardField.PHONE_HOME);
                    vCardValues.remove(VCardField.PHONE_WORK);
                    vCardValues.remove(VCardField.FAX_WORK);
                    vCardValues.remove(VCardField.MOBILE_WORK);
                    vCardValues.remove(VCardField.PAGER_WORK);
                    break;
            }
        }

        // Add new profiles that remains in the vCardValues, those are new profiles.

        if (vCardValues.containsKey(VCardField.TITLE)) {
            String newValue = vCardValues.get(VCardField.TITLE);
            addProfile(profiles, ClearspaceField.TITLE, newValue);
        }

        if (vCardValues.containsKey(VCardField.ORG_ORGUNIT)) {
            String newValue = vCardValues.get(VCardField.ORG_ORGUNIT);
            addProfile(profiles, ClearspaceField.DEPARTMENT, newValue);
        }

        if (vCardValues.containsKey(VCardField.ADR_WORK)) {
            String newValue = vCardValues.get(VCardField.ADR_WORK);
            addProfile(profiles, ClearspaceField.ADDRESS, newValue);
        }

        if (vCardValues.containsKey(VCardField.ADR_HOME)) {
            String newValue = vCardValues.get(VCardField.ADR_HOME);
            addProfile(profiles, ClearspaceField.HOME_ADDRESS, newValue);
        }

        if (vCardValues.containsKey(VCardField.TZ)) {
            String newValue = vCardValues.get(VCardField.TZ);
            addProfile(profiles, ClearspaceField.TIME_ZONE, newValue);
        }

        if (vCardValues.containsKey(VCardField.URL)) {
            String newValue = vCardValues.get(VCardField.URL);
            addProfile(profiles, ClearspaceField.URL, newValue);
        }

        if (vCardValues.containsKey(VCardField.EMAIL_USERID)) {
            String newValue = vCardValues.get(VCardField.EMAIL_USERID);
            newValue = addFieldType(newValue, "work");
            addProfile(profiles, ClearspaceField.ALT_EMAIL, newValue);
        }

        // Adds just one phone number, the first one. Clearspace doesn't support more than one.
        if (vCardValues.containsKey(VCardField.PHONE_WORK)) {
            String newValue = vCardValues.get(VCardField.PHONE_WORK);
            newValue = addFieldType(newValue, "work");
            addProfile(profiles, ClearspaceField.PHONE, newValue);

        } else if (vCardValues.containsKey(VCardField.PHONE_HOME)) {
            String newValue = vCardValues.get(VCardField.PHONE_HOME);
            newValue = addFieldType(newValue, "home");
            addProfile(profiles, ClearspaceField.PHONE, newValue);

        } else if (vCardValues.containsKey(VCardField.FAX_WORK)) {
            String newValue = vCardValues.get(VCardField.FAX_WORK);
            newValue = addFieldType(newValue, "fax");
            addProfile(profiles, ClearspaceField.PHONE, newValue);

        } else if (vCardValues.containsKey(VCardField.MOBILE_WORK)) {
            String newValue = vCardValues.get(VCardField.MOBILE_WORK);
            newValue = addFieldType(newValue, "mobile");
            addProfile(profiles, ClearspaceField.PHONE, newValue);

        } else if (vCardValues.containsKey(VCardField.PAGER_WORK)) {
            String newValue = vCardValues.get(VCardField.PAGER_WORK);
            newValue = addFieldType(newValue, "pager");
            addProfile(profiles, ClearspaceField.PHONE, newValue);
        }

        return action;
    }

    private void addProfile(Element profiles, ClearspaceField field, String newValue) {
        // Don't add empty vales
        if (newValue == null || "".equals(newValue.trim())) {
            return;
        }

        Element profile = profiles.addElement("profiles");
        profile.addElement("fieldID").setText(String.valueOf(field.getId()));
        if (field.isMultipleValues()) {
            profile.addElement("values").setText(newValue);
        } else {
            profile.addElement("value").setText(newValue);
        }
    }

    private boolean modifyProfileValue(Map<VCardField, String> vCardValues, Element profiles, Element value, VCardField vCardField) {
        boolean modified = false;
        String newValue = vCardValues.get(vCardField);

        // Modifies or deletes the value
        if (!value.getTextTrim().equals(newValue)) {
            value.setText(newValue == null ? "" : newValue);
            modified = true;
        }

        // Remove the vCard value to mark that it was used
        vCardValues.remove(vCardField);

        return modified;
    }

    private String addFieldType(String value, String type) {
        if (value == null || "".equals(value.trim())) {
            return null;
        }
        return value + "|" + type;
    }

    private String getFieldType(String field) {
        int i = field.indexOf("|");
        if (i == -1) {
            return null;
        } else {
            return field.substring(i + 1);
        }
    }

    private String getFieldValue(String field) {
        int i = field.indexOf("|");
        if (i == -1) {
            return field;
        } else {
            return field.substring(0, i);
        }
    }

    /**
     * Collects the vCard values and store them into a map.
     * They are stored with this constants:
     *
     * @param vCardElement
     * @return
     */
    private Map<VCardField, String> collectVCardValues(Element vCardElement) {

        Map<VCardField, String> vCardValues = new HashMap<VCardField, String>();

        // Add the Title
        vCardValues.put(VCardField.TITLE, vCardElement.elementText("TITLE"));

        // Add the Department
        Element orgElement = vCardElement.element("ORG");
        if (orgElement != null) {
            vCardValues.put(VCardField.ORG_ORGUNIT, orgElement.elementText("ORGUNIT"));
        }

        // Add the home and work address
        List<Element> addressElements = (List<Element>) vCardElement.elements("ADR");
        if (addressElements != null) {
            for (Element address : addressElements) {
                if (address.element("WORK") != null) {
                    vCardValues.put(VCardField.ADR_WORK, translateAddress(address));
                } else if (address.element("HOME") != null) {
                    vCardValues.put(VCardField.ADR_HOME, translateAddress(address));
                }
            }
        }

        // Add the URL
        vCardValues.put(VCardField.URL, vCardElement.elementText("URL"));

        // Add the preferred and alternative email address
        List<Element> emailsElement = (List<Element>) vCardElement.elements("EMAIL");
        if (emailsElement != null) {
            for (Element emailElement : emailsElement) {
                if (emailElement.element("PREF") == null) {
                    vCardValues.put(VCardField.EMAIL_USERID, emailElement.elementText("USERID"));
                } else {
                    vCardValues.put(VCardField.EMAIL_PREF_USERID, emailElement.elementText("USERID"));
                }
            }
        }

        // Add the full name
        vCardValues.put(VCardField.FN, vCardElement.elementText("FN"));

        // Add the time zone
        vCardValues.put(VCardField.TZ, vCardElement.elementText("TZ"));

        // Add the photo
        Element photoElement = vCardElement.element("PHOTO");
        if (photoElement != null) {
            vCardValues.put(VCardField.PHOTO_TYPE, photoElement.elementText("TYPE"));
            vCardValues.put(VCardField.PHOTO_BINVAL, photoElement.elementText("BINVAL"));
        }

        // Add the home and work tel
        List<Element> telElements = (List<Element>) vCardElement.elements("TEL");
        if (telElements != null) {
            for (Element tel : telElements) {
                String number = tel.elementText("NUMBER");
                if (tel.element("WORK") != null) {
                    if (tel.element("VOICE") != null) {
                        vCardValues.put(VCardField.PHONE_WORK, number);

                    } else if (tel.element("FAX") != null) {
                        vCardValues.put(VCardField.FAX_WORK, number);

                    } else if (tel.element("CELL") != null) {
                        vCardValues.put(VCardField.MOBILE_WORK, number);

                    } else if (tel.element("PAGER") != null) {
                        vCardValues.put(VCardField.PAGER_WORK, number);

                    }
                } else if (tel.element("HOME") != null && tel.element("VOICE") != null) {
                    vCardValues.put(VCardField.PHONE_HOME, number);
                }
            }
        }

        return vCardValues;
    }

    /**
     * Translates the information from Clearspace into a VCard.
     *
     * @param profile
     * @param user
     * @param avatar
     * @return
     */
    protected Element translateClearspaceInfo(Element profile, User user, Element avatar) {

        Document vCardDoc = DocumentHelper.createDocument();
        Element vCard = vCardDoc.addElement("vCard", "vcard-temp");

        translateUserInformation(user, vCard);
        translateProfileInformation(profile, vCard);
        translateAvatarInformation(avatar, vCard);

        return vCard;
    }


    private void translateProfileInformation(Element profiles, Element vCard) {
        // Translate the profile XML

        /* Profile response sample
        <ns1:getProfileResponse xmlns:ns1="http://jivesoftware.com/clearspace/webservices">
            <return>
                <fieldID>2</fieldID>
                <value>RTC</value>
            </return>
            <return>
                <fieldID>9</fieldID>
                <value>-300</value>
            </return>
            <return>
                <fieldID>11</fieldID>
                <value>street1:San Martin,street2:1650,city:Cap Fed,state:Buenos Aires,country:Argentina,zip:1602,type:HOME</value>
            </return>
            <return>
                <fieldID>1</fieldID>
                <value>Mr.</value>
            </return>
            <return>
                <fieldID>3</fieldID>
                <value>street1:Alder 2345,city:Portland,state:Oregon,country:USA,zip:32423,type:WORK</value>
            </return>
            <return>
                <fieldID>10</fieldID>
                <values>gguardin@gmail.com|work</values>
            </return>
            <return>
                <fieldID>5</fieldID>
                <values>http://www.gguardin.com.ar</values>
            </return>
        </ns1:getProfileResponse>
        */

        List<Element> profilesList = (List<Element>) profiles.elements("return");

        for (Element profileElement : profilesList) {
            long fieldID = Long.valueOf(profileElement.elementText("fieldID"));
            ClearspaceField field = ClearspaceField.valueOf(fieldID);

            // If the field is not known, skip it
            if (field == null) {
                continue;
            }

            // The value name of the value field could be value or values
            String fieldText = profileElement.elementText("value");
            if (fieldText == null) {
                fieldText = profileElement.elementText("values");
                // if it is an empty field, continue with the next field
                if (fieldText == null) {
                    continue;
                }
            }

            String fieldType = getFieldType(fieldText);
            String fieldValue = getFieldValue(fieldText);

            switch (field) {
                case TITLE:
                    vCard.addElement("TITLE").setText(fieldValue);
                    break;
                case DEPARTMENT:
                    vCard.addElement("ORG").addElement("ORGUNIT").setText(fieldValue);
                    break;
                case TIME_ZONE:
                    vCard.addElement("TZ").setText(fieldValue);
                    break;
                case ADDRESS:
                    Element workAdr = vCard.addElement("ADR");
                    workAdr.addElement("WORK");
                    translateAddress(fieldValue, workAdr);
                    break;
                case HOME_ADDRESS:
                    Element homeAdr = vCard.addElement("ADR");
                    homeAdr.addElement("HOME");
                    translateAddress(fieldValue, homeAdr);
                    break;
                case URL:
                    vCard.addElement("URL").setText(fieldValue);
                    break;
                case ALT_EMAIL:
                    fieldValue = getFieldValue(fieldValue);
                    Element email = vCard.addElement("EMAIL");
                    email.addElement("USERID").setText(fieldValue);
                    email.addElement("INTERNET").setText(fieldValue);
                    if ("work".equalsIgnoreCase(fieldType)) {
                        email.addElement("WORK");
                    } else if ("home".equalsIgnoreCase(fieldType)) {
                        email.addElement("HOME");
                    }

                    break;
                case PHONE:
                    Element tel = vCard.addElement("TEL");
                    tel.addElement("NUMBER").setText(fieldValue);

                    if ("home".equalsIgnoreCase(fieldType)) {
                        tel.addElement("HOME");
                        tel.addElement("VOICE");

                    } else if ("work".equalsIgnoreCase(fieldType)) {
                        tel.addElement("WORK");
                        tel.addElement("VOICE");

                    } else if ("fax".equalsIgnoreCase(fieldType)) {
                        tel.addElement("WORK");
                        tel.addElement("FAX");

                    } else if ("mobile".equalsIgnoreCase(fieldType)) {
                        tel.addElement("WORK");
                        tel.addElement("CELL");

                    } else if ("pager".equalsIgnoreCase(fieldType)) {
                        tel.addElement("WORK");
                        tel.addElement("PAGER");

                    } else if ("other".equalsIgnoreCase(fieldType)) {
                        // don't send
                    }
                    break;
            }
        }
    }

    private void translateUserInformation(User user, Element vCard) {
        // The name could be null (if in Clearspace the name is not visible in Openfire it is null)
        if (user.getName() != null && !"".equals(user.getName().trim())) {
            vCard.addElement("FN").setText(user.getName());
            vCard.addElement("N").addElement("FAMILY").setText(user.getName());
        }

        // Email is mandatory, but may be invisible
        if (user.getEmail() != null && !"".equals(user.getName().trim())) {
            Element email = vCard.addElement("EMAIL");
            email.addElement("PREF");
            email.addElement("USERID").setText(user.getEmail());
        }

        String jid = XMPPServer.getInstance().createJID(user.getUsername(), null).toBareJID();
        vCard.addElement("JABBERID").setText(jid);
    }

    private void translateAvatarInformation(Element avatarResponse, Element vCard) {
        Element avatar = avatarResponse.element("return");
        if (avatar != null) {
            Element attachment = avatar.element("attachment");
            if (attachment != null) {
                String contentType = attachment.elementText("contentType");
                String data = attachment.elementText("data");

                // Add the avatar to the vCard
                Element photo = vCard.addElement("PHOTO");
                photo.addElement("TYPE").setText(contentType);
                photo.addElement("BINVAL").setText(data);
            }
        }
    }

    private void translateAddress(String address, Element addressE) {
        StringTokenizer strTokenize = new StringTokenizer(address, ",");
        while (strTokenize.hasMoreTokens()) {
            String token = strTokenize.nextToken();
            int index = token.indexOf(":");
            String field = token.substring(0, index);
            String value = token.substring(index + 1);

            if ("street1".equals(field)) {
                addressE.addElement("STREET").setText(value);

            } else if ("street2".equals(field)) {
                addressE.addElement("EXTADD").setText(value);

            } else if ("city".equals(field)) {
                addressE.addElement("LOCALITY").setText(value);

            } else if ("state".equals(field)) {
                addressE.addElement("REGION").setText(value);

            } else if ("country".equals(field)) {
                addressE.addElement("CTRY").setText(value);

            } else if ("zip".equals(field)) {
                addressE.addElement("PCODE").setText(value);

            } else if ("type".equals(field)) {
                if ("HOME".equals(value)) {
                    addressE.addElement("HOME");
                } else if ("WORK".equals(value)) {
                    addressE.addElement("WORK");
                }
            }
        }

    }


    private String translateAddress(Element addressElement) {

        StringBuilder sb = new StringBuilder();

        translateAddressField(addressElement, "STREET", "street1", sb);

        translateAddressField(addressElement, "EXTADD", "street2", sb);

        translateAddressField(addressElement, "LOCALITY", "city", sb);

        translateAddressField(addressElement, "REGION", "state", sb);

        translateAddressField(addressElement, "CTRY", "country", sb);

        translateAddressField(addressElement, "PCODE", "zip", sb);

        // if there is no address return an empty string
        if (sb.length() == 0) {
            return "";
        }

        // if there is an address add the home or work type
        if (addressElement.element("HOME") != null) {
            sb.append("type:HOME");
        } else if (addressElement.element("WORK") != null) {
            sb.append("type:WORK");
        }

        return sb.toString();
    }

    private void translateAddressField(Element addressElement, String elementName, String fieldName, StringBuilder sb) {
        String field = addressElement.elementTextTrim(elementName);
        if (field != null && !"".equals(field)) {
            sb.append(fieldName).append(":").append(field).append(",");
        }
    }

}
