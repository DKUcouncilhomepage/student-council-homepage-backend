package com.dku.council.domain.page.service;

import com.dku.council.domain.page.exception.CarouselNotFoundException;
import com.dku.council.domain.page.model.CarouselImage;
import com.dku.council.domain.page.model.dto.request.RequestCarouselImageDto;
import com.dku.council.domain.page.model.dto.response.CarouselImageResponse;
import com.dku.council.domain.page.repository.CarouselImageRepository;
import com.dku.council.global.error.exception.IllegalTypeException;
import com.dku.council.infra.nhn.service.FileUploadService;
import com.dku.council.mock.MultipartFileMock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PageServiceTest {

    @Mock
    private CarouselImageRepository carouselImageRepository;

    @Mock
    private FileUploadService fileUploadService;

    @InjectMocks
    private PageServiceImpl service;

    @Test
    @DisplayName("확장자 실패 - jpg | jpeg ...")
    void notImage(){
        // given
        MultipartFile file = MultipartFileMock.create("test", "docs");
        RequestCarouselImageDto request = new RequestCarouselImageDto(file, "test/test");

        // when & then
        assertThrows(IllegalTypeException.class, () -> service.addCarouselImage(request));
    }

    @Test
    @DisplayName("확장자 성공 - jpg | jpeg ...")
    void Image(){
        // given
        MultipartFile file = MultipartFileMock.create("test", "jpg");
        RequestCarouselImageDto request = new RequestCarouselImageDto(file, "test/test");
        when(fileUploadService.uploadFile(any(), any())).thenReturn("1234");
        service.addCarouselImage(request);
    }

    @Test
    @DisplayName("이미지 가져오기")
    void get(){
        MultipartFile file = MultipartFileMock.create("test", "jpg");
        RequestCarouselImageDto request = new RequestCarouselImageDto(file, "test/test");
        when(fileUploadService.uploadFile(any(), any())).thenReturn("1234");
        when(carouselImageRepository.findAll()).thenReturn(List.of(
                CarouselImage.builder()
                        .redirectUrl("test/test")
                        .fileId("1234")
                        .build()
        ));
        service.addCarouselImage(request);

        List<CarouselImageResponse> carouselImages = service.getCarouselImages();
        CarouselImageResponse carouselImageResponse = carouselImages.get(0);

        assertThat(carouselImageResponse.getRedirectUrl()).isEqualTo("test/test");
    }

    @Test
    @DisplayName("이미지 삭제")
    void delete(){
        // when
        CarouselImage carouselImage = CarouselImage.builder()
                .redirectUrl("test/test")
                .fileId("1234")
                .build();

        //given
        when(carouselImageRepository.findById(carouselImage.getId())).thenReturn(Optional.of(carouselImage));
        when(carouselImageRepository.findById(100L)).thenThrow(CarouselNotFoundException.class);

        //then
        assertThrows(CarouselNotFoundException.class, () -> service.deleteCarouselImage(100L));
        service.deleteCarouselImage(carouselImage.getId());
    }

}
