package codesquad.adapter;

import codesquad.annotation.RequestMapping;
import codesquad.http.*;
import codesquad.model.MultipartFile;
import codesquad.model.dao.PostCreateDao;
import codesquad.service.PostServiceSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class PostAdapter implements Adapter {
    private static final Logger logger = LoggerFactory.getLogger(PostAdapter.class);
    private static final String STATIC_DIR = "/static";
    private static final String UPLOAD_DIR = System.getProperty("user.home") + File.separator + "uploads";
    private PostServiceSpec postService;

    public PostAdapter(PostServiceSpec postService) {
        this.postService = postService;
        createUploadDir();
    }

    private void createUploadDir() {
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            if (!uploadDir.mkdirs()) {
                logger.error("Failed to create upload directory");
            }
        }
    }

    @Override
    public boolean supports(String path) {
        return path.startsWith("/posts");
    }

    @RequestMapping(path = "/posts/create", method = HttpMethod.POST)
    public HttpResponse createPost(HttpRequest request) {
        logger.debug("createPost start");
        HttpBody body = request.getBody();
        PostCreateDao dao = body.parse(PostCreateDao.class);
        dao.setUserId(request.getHeader("userId"));
        logger.debug("PostCreateDao : {}", dao);

        if (dao.getFile() != null) {
            String savedFilePath = saveFileToLocalResource(dao.getFile());
            dao.setImageUrl(savedFilePath);  // 저장된 파일 경로를 DAO에 설정
            logger.debug("savedFilePath : {}", savedFilePath);
        }

        logger.debug("PostCreateDao : {}", dao);
        postService.createPost(dao);
        logger.debug("Post created successfully");

        return new HttpResponse.Builder(request, HttpStatus.OK)
                .build();
    }

    private String saveFileToLocalResource(MultipartFile file) {
        String uniqueFileName = UUID.randomUUID() + "_" + file.getFileName();
        File targetFile = new File(UPLOAD_DIR, uniqueFileName);
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(targetFile))) {
            bos.write(file.getContent(), 0, file.getContent().length);
        } catch (IOException e) {
            logger.error("Error saving file", e);
        }

        logger.debug("File saved at: {}", targetFile.getAbsolutePath());
        return "/uploads/" + uniqueFileName;
    }
}
