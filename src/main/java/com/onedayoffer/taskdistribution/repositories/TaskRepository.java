package com.onedayoffer.taskdistribution.repositories;

import com.onedayoffer.taskdistribution.repositories.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Integer> {

  List<Task> findAllByEmployeeId(Integer employee_id);

  Optional<Task> findByIdAndEmployeeId(Integer id, Integer employeeId);
}
