package io.linlan.tools.board.service.persist.excel;

import com.alibaba.fastjson.JSONObject;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * 
 * Filename:XlsProcessor.java
 * Desc:
 *
 * @author hcday
 * @author <a href="mailto:hcday@qq.com">hcday soo</a>
 * CreateTime:2018/1/3 12:04
 *
 * @version 1.0
 * @since 1.0
 *
 */
public class XlsProcessor {

    private JSONObject widget;
    private JSONObject data;
    private Workbook wb;
    private Sheet boardSheet;
    private CellStyle tableStyle;
    private CellStyle tStyle;
    private CellStyle titleStyle;
    private CellStyle percentStyle;
    private int c1;
    private int r1;
    private int c2;
    private int r2;

    public CellStyle getPercentStyle() {
        return percentStyle;
    }

    public void setPercentStyle(CellStyle percentStyle) {
        this.percentStyle = percentStyle;
    }

    public int getC1() {
        return c1;
    }

    public void setC1(int c1) {
        this.c1 = c1;
    }

    public int getR1() {
        return r1;
    }

    public void setR1(int r1) {
        this.r1 = r1;
    }

    public int getC2() {
        return c2;
    }

    public void setC2(int c2) {
        this.c2 = c2;
    }

    public int getR2() {
        return r2;
    }

    public void setR2(int r2) {
        this.r2 = r2;
    }

    public JSONObject getWidget() {
        return widget;
    }

    public void setWidget(JSONObject widget) {
        this.widget = widget;
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    public Workbook getWb() {
        return wb;
    }

    public void setWb(Workbook wb) {
        this.wb = wb;
    }

    public Sheet getBoardSheet() {
        return boardSheet;
    }

    public void setBoardSheet(Sheet boardSheet) {
        this.boardSheet = boardSheet;
    }

    public CellStyle getTableStyle() {
        return tableStyle;
    }

    public void setTableStyle(CellStyle tableStyle) {
        this.tableStyle = tableStyle;
    }

    public CellStyle gettStyle() {
        return tStyle;
    }

    public void settStyle(CellStyle tStyle) {
        this.tStyle = tStyle;
    }

    public CellStyle getTitleStyle() {
        return titleStyle;
    }

    public void setTitleStyle(CellStyle titleStyle) {
        this.titleStyle = titleStyle;
    }
}
