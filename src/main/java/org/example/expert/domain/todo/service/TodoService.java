package org.example.expert.domain.todo.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import static org.example.expert.domain.QTodo.todo;
import static org.example.expert.domain.QUser.user;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class TodoService {

    private final TodoRepository todoRepository;
    private final WeatherClient weatherClient;
    private final JPAQueryFactory jpaQueryFactory;

    public TodoSaveResponse saveTodo(AuthUser authUser, TodoSaveRequest todoSaveRequest) {
        User user = User.fromAuthUser(authUser);

        String weather = weatherClient.getTodayWeather();

        Todo newTodo = new Todo(
                todoSaveRequest.getTitle(),
                todoSaveRequest.getContents(),
                todoSaveRequest.getNickname(),
                weather,
                user
        );
        Todo savedTodo = todoRepository.save(newTodo);

        return new TodoSaveResponse(
                savedTodo.getId(),
                savedTodo.getTitle(),
                savedTodo.getContents(),
                savedTodo.getNickname(),
                weather,
                new UserResponse(user.getId(), user.getEmail())
        );
    }

    public Page<TodoResponse> getTodos(int page, int size , String weather, LocalDateTime startDate, LocalDateTime endDate) {
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Todo> todos = todoRepository.findByWeatherAndModifiedAtBetween(weather, startDate, endDate, pageable);

        return todos.map(todo -> new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                todo.getNickname(),
                new UserResponse(todo.getUser().getId(), todo.getUser().getEmail()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        ));
    }

    public TodoResponse getTodo(long todoId) {
        Todo foundTodo = jpaQueryFactory
                .selectFrom(todo)
                .join(todo.user, user)
                .where(todo.id.eq(todoId))
                .fetchOne();

        if (foundTodo == null) {
            throw new InvalidRequestException("Todo not found");
        }

        User foundUser = foucndTodo.getUser();

        return new TodoResponse(
                foundTodo.getId(),
                foundTodo.getTitle(),
                foundTodo.getContents(),
                foundTodo.getWeather(),
                foundTodo.getNickname(),
                new UserResponse(foundUser.getId(), foundUser.getEmail()),
                foundTodo.getCreatedAt(),
                foundTodo.getModifiedAt()
        );
    }
}
