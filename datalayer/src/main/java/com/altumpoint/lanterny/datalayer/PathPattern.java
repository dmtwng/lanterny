package com.altumpoint.lanterny.datalayer;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Path pattern for matching paths in DataLayer.
 * It is sequence of characters like:
 * <p/>
 * {@code "/domain/${id}"}
 * <p/>
 * Where {@code "domain"} is a constant characters, {@code "${id}"} - variable with name {@code id}.
 *
 * @author Dmytro Patserkovskyi
 * @since 0.1.0
 */
class PathPattern {

    private static final String VARIABLE_NAME_MATCHER = "\\w+";
    private static final String ELEMENT_MATCHER = "\\$\\{" + VARIABLE_NAME_MATCHER + "\\}";

    private String pattern;
    private String regexPattern;
    private List<PathVariableMetadata> variablesList;


    /**
     * Default constructor.
     *
     * @param pattern path pattern in format.
     */
    PathPattern(String pattern) {
        this.pattern = pattern;
        prepareRegexPattern(pattern);
    }


    /**
     * Checks if {@code path} matches for current PathPattern.
     *
     * @param path string path.
     * @return {@code true} if matches, {@code false} otherwise.
     */
    boolean matches(String path) {
        return path.matches(this.regexPattern);
    }


    /**
     * Returns map with all variables, parsed from {@code path}.
     *
     * @param path path to parse.
     * @return all variables.
     */
    public Map<String, String> parsePath(String path) {
        Map<String, String> values = new HashMap<>();
        String[] elements = path.split("/");
        for (PathVariableMetadata metadata : this.variablesList) {
            values.put(metadata.name, elements[metadata.index]);
        }
        return values;
    }


    /**
     * Checks if {@code path} equals with current path.
     *
     * @param pathPattern string path pattern.
     * @return {@code true} if equals, {@code false} otherwise.
     */
    boolean equalsPattern(String pathPattern) {
        return this.pattern.equals(pathPattern);
    }


    private void prepareRegexPattern(String pattern) {
        this.variablesList = new ArrayList<>(4);

        String[] elements = pattern.split("/");
        StringBuilder regexPatternBuilder = new StringBuilder();
        String element;
        for (int i = 1; i < elements.length; i++) {
            regexPatternBuilder.append('/');
            element = elements[i];
            if (element.matches(ELEMENT_MATCHER)) {
                PathVariableMetadata metadata = new PathVariableMetadata();
                metadata.index = i;
                metadata.name = element.substring(2, element.length() - 1);
                this.variablesList.add(metadata);

                regexPatternBuilder.append(VARIABLE_NAME_MATCHER);
            } else {
                regexPatternBuilder.append(element);
            }
        }
        this.regexPattern = regexPatternBuilder.toString();
    }



    private class PathVariableMetadata {
        int index;
        String name;
    }
}
