package com.onedayoffer.taskdistribution.services;

import com.onedayoffer.taskdistribution.DTO.EmployeeDTO;
import com.onedayoffer.taskdistribution.DTO.TaskDTO;
import com.onedayoffer.taskdistribution.DTO.TaskStatus;
import com.onedayoffer.taskdistribution.repositories.EmployeeRepository;
import com.onedayoffer.taskdistribution.repositories.TaskRepository;
import com.onedayoffer.taskdistribution.repositories.entities.Employee;
import com.onedayoffer.taskdistribution.repositories.entities.Task;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final TaskRepository taskRepository;
    private final ModelMapper modelMapper;

    public List<EmployeeDTO> getEmployees(@Nullable String sortDirection) {
        List<Employee> employees = employeeRepository.findAll(createSortObject(sortDirection));
        if (employees.isEmpty()) {
            log.warn("Employee list is empty");
        }
        return employeeToDtoList(employees);
    }

    public List<EmployeeDTO> employeeToDtoList(List<Employee> entity) {
        Type listType = new TypeToken<List<EmployeeDTO>>() {
        }.getType();
        return modelMapper.map(entity, listType);
    }

    public List<TaskDTO> taskToDtoList(List<Task> entity) {
        Type listType = new TypeToken<List<TaskDTO>>() {
        }.getType();
        return modelMapper.map(entity, listType);
    }

    private Sort createSortObject(@Nullable String sortDirection) {
        Sort.Direction direction = Sort.Direction.ASC;
        if (sortDirection != null) {
            if (sortDirection.equalsIgnoreCase("DESC")) {
                direction = Sort.Direction.DESC;
            } else if (!sortDirection.equalsIgnoreCase("ASC")) {
                log.warn("Invalid sort direction: {}", sortDirection);
            }
        }
        return Sort.by(direction, "fio");
    }

    @Transactional
    public EmployeeDTO getOneEmployee(Integer id) {

        Employee employee =
                employeeRepository
                        .findById(id)
                        .orElseThrow(
                                () -> {
                                    log.info("Employee with id {} does not exist", id);
                                    return new RuntimeException("Employee not found");
                                });
        return modelMapper.map(employee, EmployeeDTO.class);
    }

    public List<TaskDTO> getTasksByEmployeeId(Integer id) {
        List<Task> employeeTasks = taskRepository.findAllByEmployeeId(id);
        if (employeeTasks.isEmpty()) {
            log.warn("Employee tasks list is empty");
        }
        return taskToDtoList(employeeTasks);
    }

    @Transactional
    public void changeTaskStatus(Integer employeeId, Integer taskId, @Nullable TaskStatus status) {
        Optional<Task> optionalTask = taskRepository.findByIdAndEmployeeId(taskId, employeeId);
        Task task =
                optionalTask.orElseThrow(
                        () -> {
                            log.warn(
                                    "Task with ID {} for employee with ID {} does not exist", taskId, employeeId);
                            return new RuntimeException("Task not found");
                        });
        task.setStatus(status);
        taskRepository.saveAndFlush(task);
        log.info(
                "Task with ID {} for employee with ID {} has been updated to status: {}",
                taskId,
                employeeId,
                status);
    }

    @Transactional
    public void postNewTask(Integer employeeId, TaskDTO newTaskDTO) {
        Employee employee =
                employeeRepository
                        .findById(employeeId)
                        .orElseThrow(
                                () -> {
                                    log.warn("Employee with given id: {} doesn't exist", employeeId);
                                    return new RuntimeException("Employee not found");
                                });

        Task task = modelMapper.map(newTaskDTO, Task.class);
        employee.addTask(task);
        taskRepository.save(task);

        log.info("New task created for employee with ID {}: {}", employeeId, task);
    }
}
