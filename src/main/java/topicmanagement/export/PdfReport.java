package topicmanagement.export;
import java.io.*;
import java.awt.Color;
import java.text.Normalizer;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.core.io.ClassPathResource;
final class PdfReport implements AutoCloseable {
    private final PDDocument document=new PDDocument();
    private final PDType0Font regular,bold;
    private PDPageContentStream stream;
    private float y;
    private static final float MARGIN=48,WIDTH=PDRectangle.A4.getWidth()-2*MARGIN;
    PdfReport(String title) throws IOException {
        try(var input=new ClassPathResource("fonts/NotoSans-Regular.ttf").getInputStream()) {regular=PDType0Font.load(document,input);}
        try(var input=new ClassPathResource("fonts/NotoSans-Bold.ttf").getInputStream()) {bold=PDType0Font.load(document,input);}
        document.getDocumentInformation().setTitle(title);
        document.getDocumentInformation().setAuthor("Khoa Công nghệ Thông tin");
        page();line("HCMUTE · KHOA CÔNG NGHỆ THÔNG TIN",10,true);space();line(title,18,true);space();
    }
    private void page() throws IOException {
        if(stream!=null)stream.close();
        var page=new PDPage(PDRectangle.A4);document.addPage(page);
        stream=new PDPageContentStream(document,page);y=PDRectangle.A4.getHeight()-MARGIN;
    }
    void space() {y-=10;}
    void line(String text,float size,boolean strong) throws IOException {
        var font=strong?bold:regular;
        String normalized=Normalizer.normalize(text==null?"":text,Normalizer.Form.NFC);
        var safe=new StringBuilder();
        for(int cp:normalized.codePoints().toArray()) {
            if(cp=='\n')safe.append('\n');
            else if(Character.isISOControl(cp))safe.append(' ');
            else {
                String character=new String(Character.toChars(cp));
                try {font.encode(character);safe.append(character);}
                catch(IllegalArgumentException unsupported) {safe.append('?');}
            }
        }
        for(String paragraph:safe.toString().split("\n",-1)) {
            var current=new StringBuilder();
            for(String word:paragraph.split("\\s+")) {
                if(current.length()>0 && font.getStringWidth(current+" "+word)/1000*size>WIDTH) {
                    draw(current.toString(),size,font);current.setLength(0);
                }
                if(current.length()>0)current.append(' ');
                for(int cp:word.codePoints().toArray()) {
                    String character=new String(Character.toChars(cp));
                    if(font.getStringWidth(current+character)/1000*size>WIDTH) {draw(current.toString(),size,font);current.setLength(0);}
                    current.append(character);
                }
            }
            draw(current.toString(),size,font);
        }
    }
    private void draw(String text,float size,PDType0Font font) throws IOException {
        if(y-size*1.5<MARGIN+18)page();
        stream.beginText();stream.setFont(font,size);stream.setNonStrokingColor(new Color(24,47,73));
        stream.newLineAtOffset(MARGIN,y);stream.showText(text);stream.endText();y-=size*1.5;
    }
    byte[] bytes() throws IOException {
        stream.close();stream=null;
        for(int i=0;i<document.getNumberOfPages();i++) {
            try(var footer=new PDPageContentStream(document,document.getPage(i),PDPageContentStream.AppendMode.APPEND,true,true)) {
                footer.beginText();footer.setFont(regular,8);footer.newLineAtOffset(MARGIN,28);
                footer.showText("Kết quả đã công bố · Trang "+(i+1)+" / "+document.getNumberOfPages());footer.endText();
            }
        }
        var output=new ByteArrayOutputStream();document.save(output);return output.toByteArray();
    }
    @Override public void close() throws IOException {if(stream!=null)stream.close();document.close();}
}
