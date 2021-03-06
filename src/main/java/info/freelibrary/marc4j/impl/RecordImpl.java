/**
 * Copyright (C) 2004 Bas Peters
 *
 * This file is part of MARC4J
 *
 * MARC4J is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * MARC4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with MARC4J; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package info.freelibrary.marc4j.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;

/**
 * Represents a MARC record.
 *
 * @author Bas Peters
 */
public class RecordImpl implements Record {

    private Long id;

    private Leader leader;

    protected List<ControlField> controlFields;

    protected List<DataField> dataFields;

    private String type;

    /**
     * Creates a new <code>Record</code>.
     */
    public RecordImpl() {
        controlFields = new ArrayList<ControlField>();
        dataFields = new ArrayList<DataField>();
    }

    /**
     * Sets the type of this {@link Record}.
     *
     * @param type A {@link Record} type
     */
    @Override
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * Gets the type of this {@link Record}.
     *
     * @return This {@link Record}'s type
     */
    @Override
    public String getType() {
        return type;
    }

    /**
     * Sets this {@link Record}'s {@link Leader}.
     *
     * @param leader A {@link Leader} to use in this record
     */
    @Override
    public void setLeader(final Leader leader) {
        this.leader = leader;
    }

    /**
     * Gets the {@link Leader} for this {@link Record}.
     *
     * @return The {@link Leader} for this {@link Record}
     */
    @Override
    public Leader getLeader() {
        return leader;
    }

    /**
     * Adds a <code>VariableField</code> being a <code>ControlField</code> or <code>DataField</code>.
     * <p/>
     * If the <code>VariableField</code> is a control number field (001) and the record already has a control number
     * field, the field is replaced with the new instance.
     *
     * @param field the <code>VariableField</code>
     */
    @Override
    public void addVariableField(final VariableField field) {
        if (field instanceof ControlField) {
            final ControlField controlField = (ControlField) field;

            if (field.getTag().equals("001")) {
                if (getControlNumberField() == null) {
                    controlFields.add(0, controlField);
                } else {
                    controlFields.set(0, controlField);
                }
            } else {
                controlFields.add(controlField);
            }
        } else {
            dataFields.add((DataField) field);
        }
    }

    /**
     * Removes the supplied {@link VariableField}
     */
    @Override
    public void removeVariableField(final VariableField field) {
        if (field instanceof ControlField) {
            controlFields.remove(field);
        } else {
            dataFields.remove(field);
        }
    }

    /**
     * Returns the control number field or <code>null</code> if no control number field is available.
     *
     * @return ControlField - the control number field
     */
    @Override
    public ControlField getControlNumberField() {
        for (int index = 0; index < controlFields.size(); index++) {
            final ControlField field = controlFields.get(index);

            if (field.getTag().equals("001")) {
                return field;
            }
        }

        return null;
    }

    /**
     * Gets a {@link List} of {@link ControlField}s from the {@link Record}.
     */
    @Override
    public List<ControlField> getControlFields() {
        return controlFields;
    }

    /**
     * Gets a {@link List} of {@link DataField}s from the {@link Record}.
     */
    @Override
    public List<DataField> getDataFields() {
        return dataFields;
    }

    /**
     * Gets the first {@link VariableField} with the supplied tag.
     *
     * @param aTag The tag of the field to be returned
     */
    @Override
    public VariableField getVariableField(final String aTag) {
        final Iterator<? extends VariableField> iterator = getIterator(aTag);

        while (iterator.hasNext()) {
            final VariableField field = iterator.next();

            if (field.getTag().equals(aTag)) {
                return field;
            }
        }

        return null;
    }

    /**
     * Gets a {@link List} of {@link VariableField}s with the supplied tag.
     */
    @Override
    public List<VariableField> getVariableFields(final String aTag) {
        final List<VariableField> fields = new ArrayList<VariableField>();
        final Iterator<? extends VariableField> iterator = getIterator(aTag);

        while (iterator.hasNext()) {
            final VariableField field = iterator.next();

            if (field.getTag().equals(aTag)) {
                fields.add(field);
            }
        }

        return fields;
    }

    /**
     * Gets a {@link List} of {@link VariableField}s from the {@link Record}.
     */
    @Override
    public List<VariableField> getVariableFields() {
        final List<VariableField> fields = new ArrayList<VariableField>();
        Iterator<? extends VariableField> iterator = controlFields.iterator();

        while (iterator.hasNext()) {
            fields.add(iterator.next());
        }

        iterator = dataFields.iterator();

        while (iterator.hasNext()) {
            fields.add(iterator.next());
        }

        return fields;
    }

    /**
     * Gets the {@link Record}'s control number.
     */
    @Override
    public String getControlNumber() {
        final ControlField f = getControlNumberField();

        if (f == null || f.getData() == null) {
            return null;
        } else {
            return f.getData();
        }
    }

    /**
     * Gets the {@link VariableField}s in the {@link Record} with the supplied tags.
     */
    @Override
    public List<VariableField> getVariableFields(final String[] tags) {
        final List<VariableField> list = new ArrayList<VariableField>();

        for (int i = 0; i < tags.length; i++) {
            final String tag = tags[i];
            final List<VariableField> fields = getVariableFields(tag);

            if (fields.size() > 0) {
                list.addAll(fields);
            }
        }

        return list;
    }

    /**
     * Returns a string representation of this record.
     * <p/>
     * <p/>
     * Example:
     * <p/>
     * <pre>
     *
     *      LEADER 00714cam a2200205 a 4500
     *      001 12883376
     *      005 20030616111422.0
     *      008 020805s2002 nyu j 000 1 eng
     *      020   $a0786808772
     *      020   $a0786816155 (pbk.)
     *      040   $aDLC$cDLC$dDLC
     *      100 1 $aChabon, Michael.
     *      245 10$aSummerland /$cMichael Chabon.
     *      250   $a1st ed.
     *      260   $aNew York :$bMiramax Books/Hyperion Books for Children,$cc2002.
     *      300   $a500 p. ;$c22 cm.
     *      650  1$aFantasy.
     *      650  1$aBaseball$vFiction.
     *      650  1$aMagic$vFiction.
     *
     * </pre>
     *
     * @return String - a string representation of this record
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("LEADER ");
        sb.append(getLeader().toString());
        sb.append('\n');

        for (final VariableField field : getVariableFields()) {
            sb.append(field.toString());
            sb.append('\n');
        }

        return sb.toString();
    }

    /**
     * Finds all the {@link VariableField}s that match the supplied regular expression pattern.
     */
    @Override
    public List<VariableField> find(final String pattern) {
        final List<VariableField> result = new ArrayList<VariableField>();
        Iterator<? extends VariableField> i = controlFields.iterator();

        while (i.hasNext()) {
            final VariableField field = i.next();

            if (field.find(pattern)) {
                result.add(field);
            }
        }

        i = dataFields.iterator();

        while (i.hasNext()) {
            final VariableField field = i.next();

            if (field.find(pattern)) {
                result.add(field);
            }
        }

        return result;
    }

    /**
     * Finds all the {@link VariableField}s that match the supplied tag and regular expression pattern.
     */
    @Override
    public List<VariableField> find(final String tag, final String pattern) {
        final List<VariableField> result = new ArrayList<VariableField>();

        for (final VariableField field : getVariableFields(tag)) {
            if (field.find(pattern)) {
                result.add(field);
            }
        }

        return result;
    }

    /**
     * Finds all the {@link VariableField}s that match the supplied tags and regular expression pattern.
     */
    @Override
    public List<VariableField> find(final String[] tag, final String pattern) {
        final List<VariableField> result = new ArrayList<VariableField>();

        for (final VariableField field : getVariableFields(tag)) {
            if (field.find(pattern)) {
                result.add(field);
            }
        }

        return result;
    }

    /**
     * Sets the ID for this {@link Record}.
     *
     * @param id The ID for this {@link Record}
     */
    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    /**
     * Returns the ID for this {@link Record}.
     *
     * @return An ID for this {@link Record}
     */
    @Override
    public Long getId() {
        return id;
    }

    @SuppressWarnings("unchecked")
    private Iterator<? extends VariableField> getIterator(final String aTag) {
        final int tag;

        if (aTag.length() == 3) {
            try {
                tag = Integer.parseInt(aTag);

                if (tag > 0 && tag < 10) {
                    return controlFields.iterator();
                } else if (tag >= 10 && tag <= 999) {
                    return dataFields.iterator();
                }
            } catch (final NumberFormatException details) {
                // Log warning below...
            }
        }

        // TODO: log a warning here
        return ((List<VariableField>) Collections.EMPTY_LIST).iterator();
    }
}
