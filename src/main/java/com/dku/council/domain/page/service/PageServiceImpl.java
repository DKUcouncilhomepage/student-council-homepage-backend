package com.dku.council.domain.page.service;

import com.dku.council.domain.page.exception.CarouselNotFoundException;
import com.dku.council.domain.page.model.CarouselImage;
import com.dku.council.domain.page.model.dto.PetitionSummary;
import com.dku.council.domain.page.model.dto.PostSummary;
import com.dku.council.domain.page.model.dto.request.RequestCarouselImageDto;
import com.dku.council.domain.page.model.dto.response.CarouselImageResponse;
import com.dku.council.domain.page.model.dto.response.MainPageResponseDto;
import com.dku.council.domain.page.repository.CarouselImageRepository;
import com.dku.council.domain.post.model.entity.posttype.Conference;
import com.dku.council.domain.post.model.entity.posttype.News;
import com.dku.council.domain.post.model.entity.posttype.Petition;
import com.dku.council.domain.post.repository.ConferenceRepository;
import com.dku.council.domain.post.repository.NewsRepository;
import com.dku.council.domain.post.repository.PetitionRepository;
import com.dku.council.global.error.exception.IllegalTypeException;
import com.dku.council.infra.nhn.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PageServiceImpl implements PageService{
    private final FileUploadService fileUploadService;
    private final CarouselImageRepository carouselImageRepository;
    private final PetitionRepository petitionRepository;
    private final NewsRepository newsRepository;
    private final ConferenceRepository conferenceRepository;

    @Override
    public List<CarouselImageResponse> getCarouselImages() {
        return carouselImageRepository.findAll().stream()
                .map(image -> new CarouselImageResponse(fileUploadService.getBaseURL(), image))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addCarouselImage(RequestCarouselImageDto dto) {
        MultipartFile file = dto.getImageFile();
        String redirectUrl = dto.getRedirectUrl();
        String originalFilename = file.getOriginalFilename();

        if(!verifyImageExtension(originalFilename)){
            throw new IllegalTypeException();
        }

        String fileId = fileUploadService.uploadFile(file, "carousel");
        CarouselImage carouselImage = CarouselImage.builder()
                .fileId(fileId)
                .redirectUrl(redirectUrl)
                .build();

        carouselImageRepository.save(carouselImage);
    }

    @Override
    @Transactional
    public void deleteCarouselImage(Long carouselId) {
        CarouselImage carouselImage = carouselImageRepository.findById(carouselId)
                .orElseThrow(CarouselNotFoundException::new);

        fileUploadService.deleteFile(carouselImage.getFileId());
        carouselImageRepository.delete(carouselImage);
    }

    @Override
    public MainPageResponseDto getMainPage() {
        List<PetitionSummary> petitions = petitionRepository.findTop5ByOrderByCreatedAtDesc().stream()
                .map(PetitionSummary::new).collect(Collectors.toList());
        List<PostSummary> news = newsRepository.findTop5ByOrderByCreatedAtDesc().stream()
                .map(PostSummary::new).collect(Collectors.toList());
        List<PostSummary> conferences = conferenceRepository.findTop5ByOrderByCreatedAtDesc().stream()
                .map(PostSummary::new).collect(Collectors.toList());

        List<CarouselImageResponse> carouselImages = getCarouselImages();

        return new MainPageResponseDto(carouselImages, news, conferences, petitions);
    }
    private boolean verifyImageExtension(String originName) {
        if(originName == null) return false;

        return  originName.endsWith(".jpg") ||
                originName.endsWith(".jpeg") ||
                originName.endsWith(".png") ||
                originName.endsWith(".gif") ||
                originName.endsWith(".svg") ||
                originName.endsWith(".webp");
    }
}
