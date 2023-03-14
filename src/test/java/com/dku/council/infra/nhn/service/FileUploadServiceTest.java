package com.dku.council.infra.nhn.service;

import com.dku.council.infra.nhn.model.UploadedFile;
import com.dku.council.infra.nhn.service.impl.FileUploadServiceImpl;
import com.dku.council.infra.nhn.service.impl.NHNAuthServiceImpl;
import com.dku.council.infra.nhn.service.impl.ObjectStorageServiceImpl;
import com.dku.council.mock.MultipartFileMock;
import com.dku.council.mock.UploadedFileMock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileUploadServiceTest {

    private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-f]{8}\\b-[0-9a-f]{4}\\b-[0-9a-f]{4}\\b-[0-9a-f]{4}\\b-[0-9a-f]{12}$");

    @Mock
    private NHNAuthServiceImpl authService;

    @Mock
    private ObjectStorageServiceImpl storageService;

    @InjectMocks
    private FileUploadServiceImpl service;


    @Test
    @DisplayName("upload 로직 검증")
    public void uploadFiles() {
        // given
        String prefix = "prefix";
        String ext = "txt";
        final int totalFiles = 10;

        List<MultipartFile> files = MultipartFileMock.createList(totalFiles, ext);
        when(authService.requestToken()).thenReturn("token");
        when(storageService.isInObject(any())).thenReturn(false);

        // when
        ArrayList<UploadedFile> uploadedFiles = service.uploadFiles(files, prefix);

        // then
        for (int i = 1; i <= totalFiles; i++) {
            UploadedFile file = uploadedFiles.get(i - 1);
            assertFileId(file.getFileId(), prefix, ext);
            assertThat(file.getOriginalName()).isEqualTo("myFile" + i + ".txt");
        }
    }

    @Test
    @DisplayName("upload 로직 검증 - 단일")
    public void uploadFile(){
        //given
        String title = "test";
        String ext = "txt";
        MultipartFile file = MultipartFileMock.create(title, ext);

        when(authService.requestToken()).thenReturn("token");

        //when
        String fileId = service.uploadFile(file, "test");

        //then
        assertFileId(fileId, "test", ext);
    }

    private void assertFileId(String fileId, String prefix, String ext) {
        int firstIndexDash = fileId.indexOf('-');
        int lastIndexDot = fileId.lastIndexOf('.');

        assertThat(fileId.substring(0, firstIndexDash)).isEqualTo(prefix);
        assertThat(fileId.substring(firstIndexDash + 1, lastIndexDot)).matches(UUID_PATTERN);
        assertThat(fileId.substring(lastIndexDot + 1)).isEqualTo(ext);
    }

    @Test
    @DisplayName("upload시 object storage 호출이 정확한가?")
    public void uploadFilesCallProperly() {
        // given
        final int totalFiles = 10;
        List<MultipartFile> files = MultipartFileMock.createList(totalFiles, "txt");
        when(authService.requestToken()).thenReturn("token");
        when(storageService.isInObject(any())).thenReturn(false);

        // when
        service.uploadFiles(files, "prefix");

        // then
        verify(storageService, times(totalFiles)).uploadObject(eq("token"), any(), any());
    }

    @Test
    @DisplayName("delete시 object storage 호출이 정확한가?")
    public void deleteFilesCallProperly() {
        // given
        final int totalFiles = 10;
        List<UploadedFile> files = UploadedFileMock.createList(totalFiles);
        when(authService.requestToken()).thenReturn("token");

        // when
        service.deletePostFiles(files);

        // then
        verify(storageService, times(totalFiles)).deleteObject(eq("token"), any());
    }
}