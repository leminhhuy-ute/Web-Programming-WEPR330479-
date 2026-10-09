package topicmanagement.export;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.math.BigDecimal;
import java.util.List;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
class ExportRenderingTest {
    @Test void excelPreservesVietnameseAndTreatsFormulaLikeInputAsText() throws Exception {
        var row=new ResultExportService.ResultRow("Đợt khóa luận","Nhóm 01","00123","Lê Minh Huy","KL01",
            "=HYPERLINK(\"https://example.test\")","Hội đồng",new BigDecimal("8.50"),List.of());
        try(var workbook=new XSSFWorkbook(new ByteArrayInputStream(ResultExportService.excelRows(List.of(row))))) {
            var sheet=workbook.getSheetAt(0);assertEquals("Lê Minh Huy",sheet.getRow(1).getCell(3).getStringCellValue());
            assertEquals(CellType.STRING,sheet.getRow(1).getCell(5).getCellType());
            assertEquals("00123",sheet.getRow(1).getCell(2).getStringCellValue());
            assertEquals(CellType.NUMERIC,sheet.getRow(1).getCell(7).getCellType());
            assertEquals(8.5,sheet.getRow(1).getCell(7).getNumericCellValue());assertNotNull(sheet.getPaneInformation());
        }
    }
    @Test void pdfWrapsLongCommentsAndPaginatesWithoutLosingVietnamese() throws Exception {
        byte[] bytes;
        try(var pdf=new PdfReport("KẾT QUẢ ĐỀ TÀI")) {
            pdf.line("Sinh viên: Lê Minh Huy",12,true);
            for(int i=0;i<120;i++)pdf.line("Nhận xét "+i+": Đề tài quản lý sinh viên có nội dung rõ ràng và đạt yêu cầu kiểm thử.",10,false);
            pdf.line("Chuỗi dài: "+"a".repeat(500),10,false);pdf.line("Kết thúc 🙂",10,false);bytes=pdf.bytes();
        }
        try(var document=Loader.loadPDF(bytes)) {
            assertTrue(document.getNumberOfPages()>1);
            var text=new PDFTextStripper().getText(document);assertTrue(text.contains("Lê Minh Huy"));
            assertTrue(text.contains("Nhận xét 119"));assertTrue(text.contains("Kết thúc"));
            assertTrue(text.contains("Trang 1 / "+document.getNumberOfPages()));
        }
        var path=java.nio.file.Path.of("target","export-qa.pdf");java.nio.file.Files.write(path,bytes);
    }
}
