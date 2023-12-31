package io.linlan.tools.data.aggregator.jvm;

import io.linlan.tools.data.provider.InnerAggregator;
import io.linlan.commons.cache.CacheManager;
import io.linlan.commons.core.CoreException;
import io.linlan.commons.core.NumberUtils;
import io.linlan.commons.core.StringUtils;
import io.linlan.datas.core.provider.config.AggConfig;
import io.linlan.datas.core.provider.config.CompositeConfig;
import io.linlan.datas.core.provider.config.ConfigComponent;
import io.linlan.datas.core.provider.config.DimensionConfig;
import io.linlan.datas.core.provider.result.AggregateResult;
import io.linlan.datas.core.provider.result.ColumnIndex;
import io.linlan.datas.core.utils.AggregateCollector;
import io.linlan.datas.core.utils.NaturalOrderComparator;
import io.linlan.datas.core.utils.SqlHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


/**
 * 
 * Filename:JvmAggregator.java
 * Desc:
 *
 * @author hcday
 * @author <a href="mailto:hcday@qq.com">hcday soo</a>
 * CreateTime:2017/12/20 22:12
 *
 * @version 1.0
 * @since 1.0
 *
 */
@Service
@Scope("prototype")
public class JvmAggregator extends InnerAggregator {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("rawDataCache")
    protected CacheManager<String[][]> rawDataCache;

    public void loadData(String[][] data, long interval) {
        rawDataCache.put(getCacheKey(), data, interval * 1000);
    }

    public String[] queryDimVals(String columnName, AggConfig config) throws Exception {
        String[][] data = rawDataCache.get(getCacheKey());
        Map<String, Integer> columnIndex = getColumnIndex(data);
        final int fi = columnIndex.get(columnName);
        Filter rowFilter = new Filter(config, columnIndex);
        String[] filtered = Arrays.stream(data).parallel().skip(1)
                .filter(rowFilter::filter)
                .map(e -> e[fi])
                .distinct()
                .toArray(String[]::new);
        return filtered;
    }

    private Map<String, Integer> getColumnIndex(String[][] data) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < data[0].length; i++) {
            map.put(data[0][i], i);
        }
        return map;
    }

    @Override
    public String[] getColumn(boolean reload) throws Exception {
        String[][] data = rawDataCache.get(getCacheKey());
        try {
            return data[0];
        } catch (Exception e) {
            throw new CoreException("dataset is null");
        }
    }

    @Override
    public AggregateResult queryAggData(AggConfig config) throws Exception {
        String[][] data = rawDataCache.get(getCacheKey());
        Map<String, Integer> columnIndex = getColumnIndex(data);
        Filter rowFilter = new Filter(config, columnIndex);

        Stream<ColumnIndex> columns = config.getColumns().stream().map(ColumnIndex::fromDimensionConfig);
        Stream<ColumnIndex> rows = config.getRows().stream().map(ColumnIndex::fromDimensionConfig);
        List<ColumnIndex> valuesList = config.getValues().stream().map(ColumnIndex::fromValueConfig).collect(Collectors.toList());
        List<ColumnIndex> dimensionList = Stream.concat(columns, rows).collect(Collectors.toList());
        dimensionList.forEach(e -> e.setIndex(columnIndex.get(e.getName())));
        valuesList.forEach(e -> e.setIndex(columnIndex.get(e.getName())));

        Map<Dimensions, Double[]> grouped = Arrays.stream(data).skip(1).filter(rowFilter::filter)
                .collect(Collectors.groupingBy(row -> {
                    String[] ds = dimensionList.stream().map(
                            d -> row[d.getIndex()]
                    ).toArray(String[]::new);
                    return new Dimensions(ds);
                }, AggregateCollector.getCollector(valuesList)));

        String[][] result = new String[grouped.keySet().size()][dimensionList.size() + valuesList.size()];
        int i = 0;
        for (Dimensions d : grouped.keySet()) {
            result[i++] = Stream.concat(Arrays.stream(d.dimensions), Arrays.stream(grouped.get(d)).map(e -> e.toString())).toArray(String[]::new);
        }
        int dimSize = dimensionList.size();
        for (String[] row : result) {
            IntStream.range(0, dimSize).forEach(d -> {
                if (row[d] == null) row[d] = SqlHelper.NULL_STRING;
            });
        }
        dimensionList.addAll(valuesList);
        IntStream.range(0, dimensionList.size()).forEach(j -> dimensionList.get(j).setIndex(j));
        return new AggregateResult(dimensionList, result);
    }

    private class Dimensions {
        private String[] dimensions;

        public Dimensions(String[] dimensions) {
            this.dimensions = dimensions;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Dimensions that = (Dimensions) o;

            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            return Arrays.equals(dimensions, that.dimensions);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(dimensions);
        }
    }

    private class Filter {

        private List<ConfigComponent> ruleList;
        private Map<String, Integer> columnIndex;
        private Comparator comparator = new NaturalOrderComparator();

        public Filter(AggConfig config, Map<String, Integer> columnIndex) {
            ruleList = new ArrayList<>();
            if (config != null) {
                ruleList.addAll(config.getColumns());
                ruleList.addAll(config.getRows());
                ruleList.addAll(config.getFilters());
            }
            this.columnIndex = columnIndex;
        }

        private boolean checkConfigComponent(ConfigComponent component, String[] row) {
            if (component instanceof DimensionConfig) {
                return checkRule((DimensionConfig) component, row);
            } else if (component instanceof CompositeConfig) {
                CompositeConfig compositeConfig = (CompositeConfig) component;
                if (compositeConfig.getConfigComponents().size() < 1) {
                    return false;
                }
                if ("AND".equalsIgnoreCase(compositeConfig.getType())) {
                    return compositeConfig.getConfigComponents().stream().allMatch(e -> checkConfigComponent(e, row));
                } else if ("OR".equalsIgnoreCase(compositeConfig.getType())) {
                    return compositeConfig.getConfigComponents().stream().anyMatch(e -> checkConfigComponent(e, row));
                }
            }
            return false;
        }

        private boolean checkRule(DimensionConfig rule, String[] row) {
            if (rule.getValues().size() == 0 || rule.getValues().get(0) == null) {
                return true;
            }
            String v = row[columnIndex.get(rule.getColumnName())];
            String a = rule.getValues().get(0);
            String b = rule.getValues().size() >= 2 ? rule.getValues().get(1) : null;
            if (SqlHelper.NULL_STRING.equals(a)) {
                switch ((rule.getFilterType())) {
                    case "=":
                        return v == null;
                    case "≠":
                        return v != null;
                }
            }
            if (StringUtils.isEmpty(v)) {
                return false;
            }
            switch (rule.getFilterType()) {
                case "=":
                case "eq":
                    return rule.getValues().stream().anyMatch(e -> e.equals(v));
                case "≠":
                case "ne":
                    return rule.getValues().stream().allMatch(e -> !e.equals(v));
                case ">":
                    return comparator.compare(v, a) > 0;
                case "<":
                    return comparator.compare(v, a) < 0;
                case "≥":
                    return comparator.compare(v, a) >= 0;
                case "≤":
                    return comparator.compare(v, a) <= 0;
                case "(a,b]":
                    return (rule.getValues().size() >= 2) &&
                            (comparator.compare(v, a) > 0) &&
                            (comparator.compare(v, b) <= 0);
                case "[a,b)":
                    return (rule.getValues().size() >= 2) &&
                            (comparator.compare(v, a) >= 0) &&
                            (comparator.compare(v, b) < 0);
                case "(a,b)":
                    return (rule.getValues().size() >= 2) &&
                            (comparator.compare(v, a) > 0) &&
                            (comparator.compare(v, b) < 0);
                case "[a,b]":
                    return (rule.getValues().size() >= 2) &&
                            (comparator.compare(v, a) >= 0) &&
                            (comparator.compare(v, b) <= 0);
            }
            return true;
        }

        public boolean filter(String[] row) {
            boolean result = ruleList.stream().map(rule -> SqlHelper.separateNull(rule)).allMatch(rule ->
                    checkConfigComponent(rule, row)
            );
            return result;
        }

        private Comparable[] tryToNumber(String... args) {
            boolean allNumber = Arrays.stream(args).allMatch(e -> NumberUtils.isNumber(e));
            if (allNumber) {
                return Arrays.stream(args).mapToDouble(Double::parseDouble).boxed().toArray(Double[]::new);
            } else {
                return args;
            }
        }

    }


    public boolean checkExist() {
        return rawDataCache.get(getCacheKey()) != null;
    }

    public void cleanExist() {
        rawDataCache.remove(getCacheKey());
    }
}

