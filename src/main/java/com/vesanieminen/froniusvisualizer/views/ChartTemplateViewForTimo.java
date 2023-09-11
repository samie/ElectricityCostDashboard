package com.vesanieminen.froniusvisualizer.views;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vesanieminen.froniusvisualizer.components.BarChartTemplateTimo;
import com.vesanieminen.froniusvisualizer.components.DoubleLabel;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import static com.vesanieminen.froniusvisualizer.services.NordpoolSpotService.getDateOfLatestFullDayData;
import static com.vesanieminen.froniusvisualizer.services.NordpoolSpotService.getLatest7DaysMap;
import static com.vesanieminen.froniusvisualizer.util.Utils.calculateAverageOfDay;
import static com.vesanieminen.froniusvisualizer.util.Utils.calculateCheapest3HoursOfDay;
import static com.vesanieminen.froniusvisualizer.util.Utils.calculateMaximumOfDay;
import static com.vesanieminen.froniusvisualizer.util.Utils.calculateMinimumOfDay;
import static com.vesanieminen.froniusvisualizer.util.Utils.calculateSpotAveragePriceOfMonth;
import static com.vesanieminen.froniusvisualizer.util.Utils.fiZoneID;
import static com.vesanieminen.froniusvisualizer.util.Utils.getCombinedSpotData;
import static com.vesanieminen.froniusvisualizer.util.Utils.getNumberFormat;
import static com.vesanieminen.froniusvisualizer.views.MainLayout.URL_SUFFIX;

@Route(value = "porssisahkoryhma", layout = MainLayout.class)
@RouteAlias(value = "pörssisähköryhmä", layout = MainLayout.class)
@PageTitle("Market electricity group" + URL_SUFFIX)
public class ChartTemplateViewForTimo extends Main {

    public ChartTemplateViewForTimo() {
        //setHeight("var(--fullscreen-height)");
        //setMinHeight("300px");


        final var dateOfLatestFullData = getDateOfLatestFullDayData();
        final var day = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(getLocale()).format(dateOfLatestFullData);

        final var icon = new Image("icons/icon.png", "Liukuri");
        icon.setMaxWidth(30, Unit.PIXELS);
        icon.setMaxHeight(30, Unit.PIXELS);
        final var span = new H2("Liukuri.fi");
        span.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.TextColor.SECONDARY, LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER);
        final var h2 = new H2("%s".formatted(day));
        h2.addClassNames(LumoUtility.FontSize.XLARGE);
        final var liukuriAd = new Div(span, icon);
        liukuriAd.addClassNames(LumoUtility.Display.FLEX, LumoUtility.Gap.SMALL, LumoUtility.JustifyContent.CENTER);
        final var title = new Div(h2, liukuriAd);
        title.addClassNames(LumoUtility.FlexDirection.COLUMN, LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER, LumoUtility.Margin.Top.MEDIUM);
        add(title);

        final var barChartTemplateTimo = new BarChartTemplateTimo();
        add(barChartTemplateTimo);

        final NumberFormat numberFormat = getNumberFormat(getLocale(), 2);
        numberFormat.setMinimumFractionDigits(2);

        final var combinedSpotData = getCombinedSpotData();

        //final var entryPredicate = dayFilter(dateOfLatestFullData.getDayOfMonth(), dateOfLatestFullData.getMonthValue(), dateOfLatestFullData.getYear());
        //final var collect = instantDoubleLinkedHashMap.entrySet().stream().filter(entryPredicate).map(item -> item.getValue()).toList();

        final var averageTodayLabel = new DoubleLabel(getTranslation("Day's average"), numberFormat.format(calculateAverageOfDay(dateOfLatestFullData.toLocalDate(), combinedSpotData)) + " " + getTranslation("c/kWh"));
        final var monthAverage = calculateSpotAveragePriceOfMonth(dateOfLatestFullData.toLocalDate(), combinedSpotData);
        final var averageThisMonthLabel = new DoubleLabel(getTranslation("Average this month"), numberFormat.format(monthAverage) + " " + getTranslation("c/kWh"));
        final NumberFormat decimalFormat = getNumberFormat(getLocale(), 2);
        decimalFormat.setMinimumFractionDigits(2);
        final var min = decimalFormat.format(calculateMinimumOfDay(dateOfLatestFullData.toLocalDate(), combinedSpotData));
        final var max = decimalFormat.format(calculateMaximumOfDay(dateOfLatestFullData.toLocalDate(), combinedSpotData));
        final var lowestHighestToday = new DoubleLabel(getTranslation("Lowest / highest today"), min + " / " + max);
        final var cheapestHours = calculateCheapest3HoursOfDay(dateOfLatestFullData.toLocalDate(), getLatest7DaysMap());
        final var from = cheapestHours.from().atZone(fiZoneID).getHour();
        final var to = cheapestHours.to().atZone(fiZoneID).getHour() + 1;
        final var cheapestPeriod = new DoubleLabel(getTranslation("Cheapest 3h period"), "%s:00 - %s:00, ".formatted(from, to) + getTranslation("avg.") + " " + numberFormat.format(cheapestHours.averagePrice()));
        final var div = new Div(averageTodayLabel, averageThisMonthLabel, lowestHighestToday, cheapestPeriod);
        div.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexWrap.WRAP, LumoUtility.Width.FULL/*, LumoUtility.BorderRadius.LARGE, LumoUtility.Border.ALL, LumoUtility.BorderColor.CONTRAST_10*/);
        div.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
        add(div);

        barChartTemplateTimo.setAverage(monthAverage);
        final var showTheMonthlyAverageLineCheckbox = new Checkbox(getTranslation("Show the monthly average line"));
        showTheMonthlyAverageLineCheckbox.addValueChangeListener(e -> {
            barChartTemplateTimo.setAverageClass(e.getValue() ? "average-yellow" : "average-none");
            averageThisMonthLabel.getSpanBottom().getStyle().set("border-color", e.getValue() ? "rgb(242, 182, 50)" : "");
            averageThisMonthLabel.getSpanBottom().getStyle().set("border-width", e.getValue() ? "medium" : "");
            if (e.getValue()) {
                averageThisMonthLabel.getSpanBottom().addClassNames(LumoUtility.Border.BOTTOM);
            } else {
                averageThisMonthLabel.getSpanBottom().removeClassName(LumoUtility.Border.BOTTOM);
            }
        });
        showTheMonthlyAverageLineCheckbox.setValue(true);

        showTheMonthlyAverageLineCheckbox.addClassNames(LumoUtility.Display.FLEX, LumoUtility.JustifyContent.CENTER);
        add(showTheMonthlyAverageLineCheckbox);
    }

}