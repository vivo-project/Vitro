package edu.cornell.mannlib.vitro.webapp.search.controller;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.RDFNode;

public class SearchFilter {

    private static final String FILTER = "Filter";
    private static final String RANGE_FILTER = "RangeFilter";

    private String id;
    private String name = "";
    private String from = "";
    private String to = "";
    private String fromYear = "";
    private String toYear = "";
    private boolean isPublic = false;

    private String min = "0";
    private String max = "2000";
    private int moreLimit = 30;
    private int order = 0;
    private String field = "";
    private String endField = "";
    private String inputText = "";
    private boolean localizationRequired = false;
    private boolean multivalued = false;
    private boolean selected = false;
    private boolean input = false;
    private Map<String, FilterValue> values = new LinkedHashMap<>();
    private boolean inputRegex = false;
    private boolean facetsRequired;
    private boolean reverseFacetOrder;
    private String type = FILTER;
    private String rangeText = "";
    private String rangeInput = "";
    private boolean hidden = false;
    private Optional<Locale> locale;
    private boolean multilingual;

    public String getRangeInput() {
        return rangeInput;
    }

    public void setRangeInput(String range) {
        this.rangeInput = range;
    }

    public String getRangeText() {
        return rangeText;
    }

    public SearchFilter(String id, Optional<Locale> locale) {
        this.id = id;
        this.locale = locale;
    }

    public String getName() {
        return name;
    }

    public void setName(RDFNode rdfNode) {
        if (rdfNode != null) {
            name = rdfNode.asLiteral().getLexicalForm().trim();
        }
    }

    public void setOrder(RDFNode rdfNode) {
        if (rdfNode != null) {
            order = rdfNode.asLiteral().getInt();
        }
    }

    public Integer getOrder() {
        return order;
    }

    public String getField() {
        if (multilingual) {
            if (locale.isPresent()) {
                return locale.get().toLanguageTag() + field;
            } else {
                return Locale.getDefault().toLanguageTag() + field;
            }
        }
        return field;
    }

    public String getEndField() {
        return endField;
    }

    public void setEndField(String endField) {
        this.endField = endField;
    }

    public void addValue(FilterValue value) {
        values.put(value.getId(), value);
    }

    public FilterValue getValue(String name) {
        return values.get(name);
    }

    public Map<String, FilterValue> getValues() {
        return values;
    }

    public void setField(String fieldName) {
        field = fieldName;
    }

    public boolean contains(String valueId) {
        return values.containsKey(valueId);
    }

    public boolean isLocalizationRequired() {
        return localizationRequired;
    }

    public void setLocalizationRequired(boolean localizationRequired) {
        this.localizationRequired = localizationRequired;
    }

    public String getId() {
        return id;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean isSelected) {
        this.selected = isSelected;
    }

    public boolean isMultivalued() {
        return multivalued;
    }

    public void setMultivalued(boolean multivalued) {
        this.multivalued = multivalued;
    }

    public boolean isInput() {
        return input;
    }

    public boolean isRange() {
        return RANGE_FILTER.equals(type);
    }

    public void setInput(boolean input) {
        this.input = input;
    }

    public String getInputText() {
        return inputText;
    }

    public void setInputText(String inputText) {
        if (StringUtils.isBlank(inputText)) {
            return;
        }
        selected = true;
        this.inputText = inputText;
    }

    public void setInputRegex(boolean regex) {
        this.inputRegex = regex;
    }

    public boolean isInputRegex() {
        return inputRegex;
    }

    public void setFacetsRequired(boolean facetsRequired) {
        this.facetsRequired = facetsRequired;
    }

    public boolean isFacetsRequired() {
        return facetsRequired;
    }

    public void setType(RDFNode rdfNode) {
        String typeOntClass = rdfNode.toString();
        if (typeOntClass.contains(RANGE_FILTER)) {
            type = RANGE_FILTER;
        }
    }

    public String getType() {
        return type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String fromValue) {
        this.from = fromValue;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String toValue) {
        this.to = toValue;
    }

    public void setRangeValues(String filterRangeText) {
        if (StringUtils.isBlank(filterRangeText)) {
            return;
        }
        this.rangeInput = filterRangeText;
        String[] dates = filterRangeText.trim().split(" ");
        if (dates.length != 2) {
            return;
        }
        setFrom(dates[0]);
        setFromYear(dates[0]);
        setTo(to = dates[1]);
        setToYear(dates[1]);
        rangeText = "[" + from.trim() + " TO " + to.trim() + "]";
        selected = true;
    }

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }

    private String getYear(String timeString) {
        Instant time = Instant.parse(timeString);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy").withZone(ZoneId.systemDefault());
        String formatted = formatter.format(time);
        return formatted;
    }

    public String getFromYear() {
        return fromYear;
    }

    public void setFromYear(String fromYear) {
        this.fromYear = getYear(fromYear);
    }

    public String getToYear() {
        return toYear;
    }

    public void setToYear(String toYear) {
        this.toYear = getYear(toYear);
    }

    public void sortValues() {
        List<Entry<String, FilterValue>> list = new LinkedList<>(values.entrySet());
        list.sort(new FilterValueComparator());
        values = list.stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> b,
                LinkedHashMap::new));
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    private class FilterValueComparator implements Comparator<Map.Entry<String, FilterValue>> {
        public int compare(Entry<String, FilterValue> obj1, Entry<String, FilterValue> obj2) {
            FilterValue first = obj1.getValue();
            FilterValue second = obj2.getValue();
            // sort by order first
            int result = first.getOrder().compareTo(second.getOrder());
            if (result == 0) {
                // order are equal, sort by name
                result = first.getName().toLowerCase().compareTo(second.getName().toLowerCase());
                if (result == 0) {
                    // names are equal, sort by id
                    result = first.getId().toLowerCase().compareTo(second.getId().toLowerCase());
                }
                if (reverseFacetOrder) {
                    result = -result;
                }
            }
            return result;
        }
    }

    public void removeValuesWithZeroCount() {
        Iterator<Entry<String, FilterValue>> iterator = values.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, FilterValue> entry = iterator.next();
            FilterValue value = entry.getValue();
            if (value.getCount() == 0) {
                iterator.remove();
            }
        }
    }

    public boolean isEmpty() {
        if (values.size() > 0 || isInput() || isRange()) {
            return false;
        }
        return true;
    }

    public void setHidden(boolean b) {
        this.hidden = b;
    }

    public boolean isHidden() {
        return hidden;
    }

    public int getMoreLimit() {
        return moreLimit;
    }

    public void setMoreLimit(int moreLimit) {
        this.moreLimit = moreLimit;
    }

    public void setMulitlingual(boolean multilingual) {
        this.multilingual = multilingual;
    }

    public void setReverseFacetOrder(boolean reverseFacetOrder) {
        this.reverseFacetOrder = reverseFacetOrder;
    }
}
