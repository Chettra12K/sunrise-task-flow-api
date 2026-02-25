# Task: Build a Task Management REST API

## Objective

Extend the existing Spring Boot project to build a full CRUD REST API for managing tasks.
By the end, you should have all endpoints working and be able to verify them using the validation script.

---

## What You Will Build

A `Task` API with these endpoints:

| Method | URL                        | Description            | Status Code    |
|--------|----------------------------|------------------------|----------------|
| GET    | `/api/tasks`               | List all tasks         | 200            |
| GET    | `/api/tasks/{id}`          | Get task by ID         | 200 / 404      |
| POST   | `/api/tasks`               | Create a new task      | 201 Created    |
| PUT    | `/api/tasks/{id}`          | Update a task          | 200 / 404      |
| PATCH  | `/api/tasks/{id}/complete` | Mark task as complete  | 200 / 404      |
| DELETE | `/api/tasks/{id}`          | Delete a task          | 204 / 404      |
| GET    | `/api/tasks?completed=true`| Filter by status       | 200            |

---

## Step 1: Create the Task Model

Create `src/main/java/.../model/Task.java` with these fields:

| Field | Type | Notes |
|-------|------|-------|
| `id` | `Long` | auto-assigned |
| `title` | `String` | |
| `description` | `String` | |
| `completed` | `boolean` | defaults to `false` |
| `createdAt` | `LocalDateTime` | set at creation time |

Use Lombok (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`).

Also add a convenience constructor that takes only `(Long id, String title, String description)` and sets `completed = false` and `createdAt = LocalDateTime.now()` automatically.

---

## Step 2: Create the Request DTO

Create `src/main/java/.../dto/TaskRequest.java`:

```java
@Data
public class TaskRequest {
    private String title;
    private String description;
}
```

This is what the client sends in `POST` and `PUT` request bodies.

---

## Step 3: Create a Mock Repository

Since we don't have a database yet, create `src/main/java/.../repository/TaskRepository.java` to act as an in-memory data store. The service will use this instead of talking to a real DB.

```java
@Repository
public class TaskRepository {

    private final Map<Long, Task> store = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    // Pre-loaded mock data so the API isn't empty on startup
    public TaskRepository() {
        Task t1 = new Task(nextId.getAndIncrement(), "Buy groceries", "Milk, eggs, bread");
        Task t2 = new Task(nextId.getAndIncrement(), "Read a book", "Finish Clean Code");
        Task t3 = new Task(nextId.getAndIncrement(), "Exercise", "30 min run");
        t3.setCompleted(true);
        store.put(t1.getId(), t1);
        store.put(t2.getId(), t2);
        store.put(t3.getId(), t3);
    }

    public List<Task> findAll() {
        return new ArrayList<>(store.values());
    }

    public Optional<Task> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public Task save(Task task) {
        if (task.getId() == null) {
            task.setId(nextId.getAndIncrement());
        }
        store.put(task.getId(), task);
        return task;
    }

    public boolean delete(Long id) {
        return store.remove(id) != null;
    }
}
```

The service layer will call this repository — not the `Map` directly.

---

## Step 4: Create the TaskService Interface

Create `src/main/java/.../services/TaskService.java`:

```java
public interface TaskService {
    List<Task> findAll();
    Optional<Task> findById(Long id);
    Task create(String title, String description);
    Optional<Task> update(Long id, String title, String description);
    Optional<Task> complete(Long id);
    boolean delete(Long id);
}
```

---

## Step 5: Implement TaskServiceImpl

Create `src/main/java/.../services/TaskServiceImpl.java`:

- Annotate with `@Service`
- Inject `TaskRepository` via constructor
- Implement all methods from the interface by delegating to the repository

Key logic:
- `create` — build a new `Task` using the convenience constructor, call `repository.save(task)`
- `update` — call `repository.findById(id)`, update fields, call `repository.save(task)`
- `complete` — call `repository.findById(id)`, set `completed = true`, call `repository.save(task)`
- `delete` — call `repository.delete(id)`, return the boolean result

---

## Step 6: Create the TaskController

Create `src/main/java/.../controllers/TaskController.java`:

- Annotate with `@RestController` and `@RequestMapping("/api/tasks")`
- Inject `TaskService` via constructor
- Implement all 7 endpoints from the table above

Rules:
- `POST /api/tasks` must return **201 Created** with a `Location` header
- `GET /api/tasks/{id}` must return **404** if task not found
- `DELETE /api/tasks/{id}` must return **204 No Content** on success, **404** if not found
- `PATCH /api/tasks/{id}/complete` must return **404** if task not found

### Filtering with @RequestParam

For `GET /api/tasks`, add an optional `completed` query parameter:

```java
@GetMapping
public List<Task> getAllTasks(
    @RequestParam(required = false) Boolean completed) {
    return taskService.findAll().stream()
        .filter(t -> completed == null || t.isCompleted() == completed)
        .toList();
}
```

---

## Step 6: Run and Validate

Run the provided JUnit tests — no need to start the server manually:

```bash
./mvnw test -Dtest=TaskControllerTest
```

All 14 tests should pass. Each test is isolated: Spring context resets between tests so your in-memory data never leaks between cases.

If a test fails, read the assertion error carefully — it tells you exactly which endpoint returned the wrong status code or response body.

---

## Checklist Before Running Tests

- [ ] `Task` model created with all 5 fields
- [ ] `TaskRequest` DTO created
- [ ] `TaskRepository` created with mock data and `save`/`findAll`/`findById`/`delete` methods
- [ ] `TaskService` interface defined with all 6 methods
- [ ] `TaskServiceImpl` injects `TaskRepository` and implements all methods
- [ ] `TaskController` has all 7 endpoints
- [ ] POST returns 201 with body
- [ ] GET by ID returns 404 for missing tasks
- [ ] DELETE returns 204 on success
- [ ] PATCH /complete marks task as completed
- [ ] GET supports `?completed=` filter

## Tests Overview

| Test | What it checks |
|------|---------------|
| `getAllTasks_initially_returnsEmptyList` | GET returns `[]` on fresh start |
| `getAllTasks_afterCreatingTwo_returnsBothTasks` | GET returns all created tasks |
| `createTask_validRequest_returns201WithBody` | POST returns 201, full body with all fields |
| `getTaskById_existingTask_returns200` | GET by ID returns correct task |
| `getTaskById_nonExistingId_returns404` | GET with unknown ID returns 404 |
| `updateTask_existingTask_returns200WithUpdatedFields` | PUT updates title and description |
| `updateTask_nonExistingId_returns404` | PUT with unknown ID returns 404 |
| `completeTask_existingTask_returns200AndCompletedIsTrue` | PATCH sets `completed=true` |
| `completeTask_nonExistingId_returns404` | PATCH with unknown ID returns 404 |
| `deleteTask_existingTask_returns204` | DELETE returns 204 No Content |
| `deleteTask_existingTask_isNoLongerRetrievable` | Deleted task returns 404 on GET |
| `deleteTask_nonExistingId_returns404` | DELETE with unknown ID returns 404 |
| `getAllTasks_filterCompletedTrue_returnsOnlyCompletedTasks` | `?completed=true` filters correctly |
| `getAllTasks_filterCompletedFalse_returnsOnlyPendingTasks` | `?completed=false` filters correctly |
| `getAllTasks_noFilter_returnsAllTasks` | No filter returns all tasks |
