package io.linlan.tools.board.service;

import com.alibaba.fastjson.JSONObject;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import io.linlan.tools.board.entity.DashOperationJob;
import io.linlan.tools.board.service.persist.excel.XlsProcessorService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import io.linlan.tools.board.service.persist.PersistContext;
import io.linlan.commons.script.json.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 
 * Filename:DashboardMailService.java
 * Desc:
 *
 * @author hcday
 * @author <a href="mailto:hcday@qq.com">hcday soo</a>
 * CreateTime:2018/1/3 12:02
 *
 * @version 1.0
 * @since 1.0
 *
 */
@Service
public class DashMailService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private XlsProcessorService xlsProcessorService;

    @Autowired
    private DataPersistService dataPersistService;

    @Value("${mail.smtp.host}")
    private String mail_smtp_host;

    @Value("${mail.smtp.port}")
    private Integer mail_smtp_port;

    @Value("${mail.smtp.username:#{null}}")
    private String mail_smtp_username;

    @Value("${mail.smtp.password:#{null}}")
    private String mail_smtp_password;

    @Value("${mail.smtp.from}")
    private String mail_smtp_from;

    @Value("${mail.smtp.ssl.checkserveridentity:false}")
    private Boolean mail_smtp_ssl_check;

    @Value("${mail.smtp.startTLSEnabled:false}")
    private Boolean mail_smtp_start_tls_enabled;


    private Function<Object, PersistContext> getPersistBoard(List<PersistContext> persistContextList) {
        return e -> persistContextList.stream()
                .filter(board -> {
                    String boardId = board.getDashboardId();
                    return boardId != null && boardId.equals((((JSONObject) e).getString("id")));
                })
                .findFirst().get();
    }

    public String sendDashboard(DashOperationJob job) throws Exception {
        JSONObject config = JsonUtils.parseJO(job.getConfig());

        List<PersistContext> persistContextList = config.getJSONArray("boards").stream()
                .map(e -> dataPersistService.persist(((JSONObject) e).getString("id"), job.getUserId().toString()))
                .collect(Collectors.toList());

        List<PersistContext> workbookList = config.getJSONArray("boards").stream()
                .filter(e -> ((JSONObject) e).getString("type").contains("xls"))
                .map(getPersistBoard(persistContextList))
                .collect(Collectors.toList());

        ByteArrayOutputStream baos = null;
        if (workbookList != null && workbookList.size() > 0) {
            HSSFWorkbook workbook = xlsProcessorService.dashboardToXls(workbookList);
            if (StringUtils.isNotEmpty(config.getString("xlspwd"))) {
                Biff8EncryptionKey.setCurrentUserPassword(config.getString("xlspwd"));
            }
            baos = new ByteArrayOutputStream();
            try {
                workbook.write(baos);
            } catch (IOException e) {
                logger.error("", e);
            }
        }


        List<PersistContext> picList = config.getJSONArray("boards").stream()
                .filter(e -> ((JSONObject) e).getString("type").contains("img"))
                .map(getPersistBoard(persistContextList))
                .collect(Collectors.toList());

        HtmlEmail email = new HtmlEmail();
        StringBuilder sb = new StringBuilder("<html>");
        picList.stream().forEach(e -> {
            String b64 = e.getData().getString("img");
            byte[] bytes = Base64.getDecoder().decode(b64.substring(23));
            ByteArrayDataSource ds = new ByteArrayDataSource(bytes, "application/octet-stream");
            try {
                String cid = email.embed(ds, e.getDashboardId() + ".jpg");
                sb.append("<img src='cid:").append(cid).append("'></img></br>");
            } catch (EmailException e1) {
                logger.error("", e);
            }
        });
        email.setHtmlMsg(sb.append("</html>").toString());
        email.setTextMsg("Your email client does not support HTML messages");
        if (baos != null) {
            ByteArrayDataSource ds = new ByteArrayDataSource(baos.toByteArray(), "application/octet-stream");
            email.attach(ds, "report.xls", EmailAttachment.ATTACHMENT, "test");
        }
        email.setHostName(mail_smtp_host);
        email.setSmtpPort(mail_smtp_port);
        email.setSSLCheckServerIdentity(mail_smtp_ssl_check);
        email.setStartTLSEnabled(mail_smtp_start_tls_enabled);
        if (mail_smtp_username != null && mail_smtp_password != null) {
            email.setAuthentication(mail_smtp_username, mail_smtp_password);
        }
        email.setFrom(mail_smtp_from);
        email.setSubject(config.getString("subject"));
        String to = config.getString("to");
        if (StringUtils.isNotBlank(to)) {
            if (to.contains(";")) {
                email.addTo(to.split(";"));
            } else {
                email.addTo(to);
            }
        }
        String cc = config.getString("cc");
        if (StringUtils.isNotBlank(cc)) {
            if (cc.contains(";")) {
                email.addCc(cc.split(";"));
            } else {
                email.addCc(cc);
            }
        }
        String bcc = config.getString("bcc");
        if (StringUtils.isNotBlank(bcc)) {
            if (bcc.contains(";")) {
                email.addBcc(bcc.split(";"));
            } else {
                email.addBcc(bcc);
            }
        }
        email.send();
        return null;
    }

}
