package topicmanagement.export;
import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import topicmanagement.council.*;
import topicmanagement.dto.response.StudentResultResponse;
import topicmanagement.dto.response.CouncilStateResponse.GradeView;
import topicmanagement.enums.Role;
import topicmanagement.repository.*;
import topicmanagement.security.CurrentAccount;
import topicmanagement.service.StudentResultService;
@Service @Transactional(readOnly=true)
public class ResultExportService {
    private final CurrentAccount accounts;
    private final DefenseRepository defenses;
    private final GroupMemberRepository members;
    private final GradeRepository grades;
    private final RegistrationPeriodRepository periods;
    private final StudentResultService studentResults;
    public record ResultRow(String period,String group,String studentCode,String studentName,String topicCode,
        String topic,String council,BigDecimal score,List<GradeView> grades) {}
    public ResultExportService(CurrentAccount accounts,DefenseRepository defenses,GroupMemberRepository members,
            GradeRepository grades,RegistrationPeriodRepository periods,StudentResultService studentResults) {
        this.accounts=accounts;this.defenses=defenses;this.members=members;this.grades=grades;this.periods=periods;this.studentResults=studentResults;
    }
    public List<ResultRow> rows(Long periodId) {
        if(accounts.user().getRole()!=Role.DEAN)throw new AccessDeniedException("Chỉ trưởng khoa được xuất bảng điểm.");
        if(periodId!=null&&!periods.existsById(periodId))throw new IllegalArgumentException("Đợt đăng ký không tồn tại.");
        var visible=defenses.findPublished(periodId);
        var roster=members.findByGroupIdIn(visible.stream().map(d->d.group.getId()).toList()).stream()
            .collect(Collectors.groupingBy(m->m.getGroup().getId()));
        var gradeMap=grades.findByDefenseIdIn(visible.stream().map(d->d.id).toList()).stream()
            .collect(Collectors.groupingBy(g->g.defense.id));
        var rows=new ArrayList<ResultRow>();
        for(var d:visible) {
            var topic=d.topic!=null?d.topic:d.group.getTopic();
            var evaluations=gradeMap.getOrDefault(d.id,List.of()).stream()
                .sorted(Comparator.comparing(g->g.evaluator.getFullName()))
                .map(g->new GradeView(g.evaluator.getFullName(),g.score,g.comment)).toList();
            for(var m:roster.getOrDefault(d.group.getId(),List.of()).stream()
                    .sorted(Comparator.comparing(m->m.getStudent().getUserCode())).toList()) {
                rows.add(new ResultRow(topic.getPeriod().getName(),d.group.getGroupName(),m.getStudent().getUserCode(),
                    m.getStudent().getFullName(),topic.getTopicCode(),topic.getTitle(),d.council.getName(),d.finalScore,evaluations));
            }
        }
        return List.copyOf(rows);
    }
    public byte[] excel(Long periodId) throws IOException {return excelRows(rows(periodId));}
    static byte[] excelRows(List<ResultRow> rows) throws IOException {
        try(var workbook=new XSSFWorkbook();var output=new ByteArrayOutputStream()) {
            var sheet=workbook.createSheet("Kết quả đã công bố");
            var headerStyle=workbook.createCellStyle();headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);headerStyle.setWrapText(true);
            var font=workbook.createFont();font.setBold(true);font.setColor(IndexedColors.WHITE.getIndex());headerStyle.setFont(font);
            var textStyle=workbook.createCellStyle();textStyle.setWrapText(true);textStyle.setVerticalAlignment(VerticalAlignment.TOP);
            var scoreStyle=workbook.createCellStyle();scoreStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
            var headers=List.of("Đợt","Nhóm","Mã sinh viên","Họ tên","Mã đề tài","Tên đề tài","Hội đồng","Điểm cuối cùng","Điểm thành phần");
            var header=sheet.createRow(0);header.setHeightInPoints(32);
            for(int i=0;i<headers.size();i++){var cell=header.createCell(i);cell.setCellValue(headers.get(i));cell.setCellStyle(headerStyle);}
            int index=1;
            for(var item:rows) {
                var row=sheet.createRow(index++);row.setHeightInPoints(45);
                var values=List.of(item.period(),item.group(),item.studentCode(),item.studentName(),item.topicCode(),item.topic(),item.council());
                for(int col=0;col<values.size();col++){var cell=row.createCell(col,CellType.STRING);cell.setCellValue(values.get(col));cell.setCellStyle(textStyle);}
                var score=row.createCell(7);if(item.score()!=null)score.setCellValue(item.score().doubleValue());score.setCellStyle(scoreStyle);
                var components=row.createCell(8,CellType.STRING);
                components.setCellValue(item.grades().stream().map(g->g.name()+": "+g.score()).collect(Collectors.joining("; ")));components.setCellStyle(textStyle);
            }
            int[] widths={26,24,18,28,18,55,30,18,55};for(int i=0;i<widths.length;i++)sheet.setColumnWidth(i,widths[i]*256);
            sheet.createFreezePane(0,1);sheet.setAutoFilter(new CellRangeAddress(0,Math.max(0,index-1),0,8));
            sheet.setRepeatingRows(new CellRangeAddress(0,0,-1,-1));sheet.setFitToPage(true);sheet.getPrintSetup().setLandscape(true);
            workbook.write(output);return output.toByteArray();
        }
    }
    public byte[] pdf(Long periodId) throws IOException {
        var rows=rows(periodId);
        try(var pdf=new PdfReport("BẢNG KẾT QUẢ ĐỀ TÀI")) {
            pdf.line("Thời gian xuất: "+LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),10,false);
            pdf.line("Số sinh viên: "+rows.size(),10,false);pdf.space();
            if(rows.isEmpty())pdf.line("Chưa có kết quả đã công bố trong phạm vi đã chọn.",11,false);
            for(var row:rows) {
                pdf.line(row.studentCode()+" · "+row.studentName(),12,true);
                pdf.line("Đợt: "+row.period()+" | Nhóm: "+row.group(),10,false);
                pdf.line("Đề tài: "+row.topicCode()+" · "+row.topic(),10,false);
                pdf.line("Hội đồng: "+row.council(),10,false);
                pdf.line("Điểm cuối cùng: "+row.score()+" / 10",11,true);pdf.space();
            }
            return pdf.bytes();
        }
    }
    public byte[] studentPdf(Long periodId) throws IOException {
        var result=studentResults.current(periodId);
        if(!result.published())throw new IllegalArgumentException("Chưa có kết quả được công bố cho đợt này.");
        var student=accounts.user();
        try(var pdf=new PdfReport("KẾT QUẢ ĐỀ TÀI SINH VIÊN")) {
            pdf.line("Sinh viên: "+student.getUserCode()+" · "+student.getFullName(),12,true);
            if(periodId!=null)pdf.line("Đợt: "+periods.findById(periodId).orElseThrow().getName(),11,false);
            pdf.line("Đề tài: "+result.topic(),11,false);pdf.space();
            pdf.line("Điểm cuối cùng: "+result.score()+" / 10",16,true);pdf.space();
            pdf.line("ĐIỂM THÀNH PHẦN VÀ NHẬN XÉT",12,true);
            for(var grade:result.grades()) {
                pdf.line(grade.name()+" · "+grade.score()+" / 10",11,true);pdf.line(grade.comment(),10,false);pdf.space();
            }
            return pdf.bytes();
        }
    }
}
