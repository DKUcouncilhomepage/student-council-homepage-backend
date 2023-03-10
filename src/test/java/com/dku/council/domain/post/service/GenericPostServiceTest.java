package com.dku.council.domain.post.service;

import com.dku.council.domain.post.exception.PostNotFoundException;
import com.dku.council.domain.post.exception.UserNotFoundException;
import com.dku.council.domain.post.model.dto.request.RequestCreateNewsDto;
import com.dku.council.domain.post.model.dto.response.ResponseSingleGenericPostDto;
import com.dku.council.domain.post.model.entity.posttype.News;
import com.dku.council.domain.post.repository.GenericPostRepository;
import com.dku.council.domain.tag.service.TagService;
import com.dku.council.domain.user.model.entity.User;
import com.dku.council.domain.user.repository.UserRepository;
import com.dku.council.global.error.exception.NotGrantedException;
import com.dku.council.infra.nhn.service.FileUploadService;
import com.dku.council.mock.MultipartFileMock;
import com.dku.council.mock.NewsMock;
import com.dku.council.mock.UserMock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenericPostServiceTest {

    @Mock
    private GenericPostRepository<News> newsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TagService tagService;

    @Mock
    private ViewCountService viewCountService;

    @Mock
    private FileUploadService fileUploadService;

    @InjectMocks
    private GenericPostService<News> service;


    @Test
    @DisplayName("list??? ??? ????????????????")
    public void list() {
        // given
        List<News> allNewsList = NewsMock.createList("generic-", 20);
        Page<News> allNews = new DummyPage<>(allNewsList, 20);

        when(newsRepository.findAll((Specification<News>) any(), (Pageable) any())).thenReturn(allNews);

        // when
        Page<News> allPage = service.list(null, Pageable.unpaged());

        // then
        assertThat(allPage.getTotalElements()).isEqualTo(allNewsList.size());
        assertThat(allPage.getContent()).containsExactlyInAnyOrderElementsOf(allNewsList);
    }

    @Test
    @DisplayName("????????? ??? ????????????????")
    public void create() {
        // given
        User user = UserMock.create(99L);
        News news = NewsMock.create(user, 3L);

        List<MultipartFile> files = MultipartFileMock.createList(10);
        RequestCreateNewsDto dto = new RequestCreateNewsDto("title", "body", null, files);
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(newsRepository.save(any())).thenReturn(news);

        // when
        Long newsId = service.create(2L, dto);

        // then
        assertThat(newsId).isEqualTo(3L);

        verify(fileUploadService).uploadFiles(argThat(fileList -> {
            assertThat(fileList).isEqualTo(files);
            return true;
        }), any());

        verify(newsRepository).save(argThat(entity -> {
            assertThat(entity.getUser()).isEqualTo(user);
            return true;
        }));
    }

    @Test
    @DisplayName("????????? ???????????? ????????????")
    public void createWithTag() {
        // given
        User user = UserMock.create(99L);
        News news = NewsMock.create(user, 3L);
        List<Long> tagIds = List.of(10L, 11L, 12L, 13L);

        RequestCreateNewsDto dto = new RequestCreateNewsDto("title", "body", tagIds, List.of());
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(newsRepository.save(any())).thenReturn(news);

        // when
        Long newsId = service.create(2L, dto);

        // then
        assertThat(newsId).isEqualTo(3L);

        verify(newsRepository).save(argThat(entity -> {
            assertThat(entity.getUser()).isEqualTo(user);
            return true;
        }));
        verify(tagService).addTagsToPost(any(), eq(tagIds));
    }

    @Test
    @DisplayName("????????? ??? ????????? ????????? ??????")
    public void failedCreateByNotFoundUser() {
        // given
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        // when & then
        assertThrows(UserNotFoundException.class, () ->
                service.create(2L, new RequestCreateNewsDto("title", "body", List.of(), List.of())));
    }

    @Test
    @DisplayName("?????? ????????? ??? ????????????????")
    public void findOne() {
        // given
        News news = NewsMock.create(4L);
        when(newsRepository.findById(any())).thenReturn(Optional.of(news));

        // when
        ResponseSingleGenericPostDto dto = service.findOne(4L, 5L, "Addr");

        // then
        verify(viewCountService).increasePostViews(argThat(post -> {
            assertThat(post.getId()).isEqualTo(news.getId());
            return true;
        }), eq("Addr"));

        assertThat(dto.getId()).isEqualTo(4L);
    }

    @Test
    @DisplayName("?????? ????????? ?????? ????????? ??????")
    public void failedFindOneByNotFound() {
        // given
        when(newsRepository.findById(any())).thenReturn(Optional.empty());

        // when & then
        assertThrows(PostNotFoundException.class, () ->
                service.findOne(0L, 4L, "Addr"));
    }

    @Test
    @DisplayName("?????? ????????? ????????? ??????")
    public void failedDeleteByNotFound() {
        // given
        when(newsRepository.findById(any())).thenReturn(Optional.empty());

        // when & then
        assertThrows(PostNotFoundException.class, () ->
                service.delete(0L, 0L, false));
    }

    @Test
    @DisplayName("?????? ?????? ????????? ????????? ??????")
    public void failedDeleteByAccessDenied() {
        // given
        News news = NewsMock.create(4L);
        when(newsRepository.findById(any())).thenReturn(Optional.of(news));

        // when & then
        assertThrows(NotGrantedException.class, () ->
                service.delete(0L, 0L, false));
    }
}