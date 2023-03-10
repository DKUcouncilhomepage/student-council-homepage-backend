package com.dku.council.domain.post.controller;

import com.dku.council.common.AbstractContainerRedisTest;
import com.dku.council.common.OnlyDevTest;
import com.dku.council.domain.comment.CommentRepository;
import com.dku.council.domain.comment.CommentStatus;
import com.dku.council.domain.comment.model.dto.RequestCreateCommentDto;
import com.dku.council.domain.comment.model.entity.Comment;
import com.dku.council.domain.post.model.PetitionStatus;
import com.dku.council.domain.post.model.dto.request.RequestCreateReplyDto;
import com.dku.council.domain.post.model.entity.posttype.Petition;
import com.dku.council.domain.post.repository.GenericPostRepository;
import com.dku.council.domain.user.model.entity.User;
import com.dku.council.domain.user.repository.UserRepository;
import com.dku.council.mock.CommentMock;
import com.dku.council.mock.PetitionMock;
import com.dku.council.mock.UserMock;
import com.dku.council.mock.user.UserAuth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
@OnlyDevTest
class PetitionControllerTest extends AbstractContainerRedisTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private GenericPostRepository<Petition> postRepository;


    private final ObjectMapper objectMapper = new ObjectMapper();
    private Petition petition;
    private User user;


    @BeforeEach
    void setupUser() {
        user = UserMock.create(11L);
        user = userRepository.save(user);

        petition = PetitionMock.create(user, "title", "body");
        petition = postRepository.save(petition);

        UserAuth.withUser(user.getId());
    }


    @Test
    @DisplayName("?????? ??????")
    void create() throws Exception {
        // when
        ResultActions result = mvc.perform(get("/post/petition/" + petition.getId()))
                .andDo(print());

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("id", is(petition.getId().intValue())))
                .andExpect(jsonPath("title", is("title")))
                .andExpect(jsonPath("body", is("body")))
                .andExpect(jsonPath("author", is(user.getName())))
                .andExpect(jsonPath("mine", is(true)))
                .andExpect(jsonPath("status", is(PetitionStatus.ACTIVE.name())));
    }

    @Test
    @DisplayName("?????? ??????")
    void reply() throws Exception {
        // given
        UserAuth.withAdmin(user.getId());
        RequestCreateReplyDto dto = new RequestCreateReplyDto("hello good");

        // when
        ResultActions result = mvc.perform(post("/post/petition/reply/" + petition.getId())
                        .content(objectMapper.writeValueAsBytes(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        result.andExpect(status().isOk());
        assertThat(petition.getAnswer()).isEqualTo("hello good");
        assertThat(petition.getPetitionStatus()).isEqualTo(PetitionStatus.ANSWERED);
    }

    @Test
    @DisplayName("?????? ?????? ??????")
    void listComment() throws Exception {
        // given
        Comment comment = CommentMock.create(petition, user);
        commentRepository.save(comment);

        // when
        ResultActions result = mvc.perform(get("/post/petition/comment/" + petition.getId()))
                .andDo(print());

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("content.size()", is(1)))
                .andExpect(jsonPath("content[0].text", is(comment.getText())));
    }

    @Test
    @DisplayName("?????? ?????? ??????")
    void createComment() throws Exception {
        // given
        RequestCreateCommentDto dto = new RequestCreateCommentDto("this is comment");

        // when
        ResultActions result = mvc.perform(post("/post/petition/comment/" + petition.getId())
                        .content(objectMapper.writeValueAsBytes(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        result.andExpect(status().isOk());

        List<Comment> comments = commentRepository.findAll();
        assertThat(comments.size()).isEqualTo(1);
        assertThat(comments.get(0).getText()).isEqualTo("this is comment");
        assertThat(comments.get(0).getPost().getId()).isEqualTo(petition.getId());
    }

    @Test
    @DisplayName("?????? ?????? ?????? ?????? - ???????????? 2????????? ??????")
    void createCommentTwice() throws Exception {
        // given
        Comment comment = CommentMock.create(petition, user);
        commentRepository.save(comment);
        RequestCreateCommentDto dto = new RequestCreateCommentDto("this is comment");

        // when
        ResultActions result = mvc.perform(post("/post/petition/comment/" + petition.getId())
                        .content(objectMapper.writeValueAsBytes(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("?????? ?????? ????????? ????????? ??????????????? ??????")
    void createCommentAndStateChanges() throws Exception {
        // given
        List<User> users = UserMock.createList(150);
        users = userRepository.saveAll(users);

        List<Comment> comments = CommentMock.createList(petition, users, 150);
        commentRepository.saveAll(comments);

        RequestCreateCommentDto dto = new RequestCreateCommentDto("this is comment");

        // when
        ResultActions result = mvc.perform(post("/post/petition/comment/" + petition.getId())
                        .content(objectMapper.writeValueAsBytes(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        result.andExpect(status().isOk());
        assertThat(petition.getPetitionStatus()).isEqualTo(PetitionStatus.WAITING);
    }

    @Test
    @DisplayName("?????? ?????? ??????")
    void deleteComment() throws Exception {
        // given
        UserAuth.withAdmin(user.getId());
        Comment comment = CommentMock.create(petition, user);
        comment = commentRepository.save(comment);

        // when
        ResultActions result = mvc.perform(delete("/post/petition/comment/" + comment.getId()))
                .andDo(print());

        // then
        result.andExpect(status().isOk());

        List<Comment> comments = commentRepository.findAll();
        assertThat(comments.get(0).getStatus()).isEqualTo(CommentStatus.DELETED_BY_ADMIN);
    }
}