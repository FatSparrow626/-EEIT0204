package com.project.HR.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.employeeuser.model.EmployeeUser; // 1. 引入正確的 Entity

@Repository
// 2. 將泛型從 <EmployeeHr, Integer> 改為 <EmployeeUser, Long>
public interface HrEmployeeRepository extends JpaRepository<EmployeeUser, Long> { 

    /**
     * 根據姓名模糊查詢員工 (忽略大小寫)。
     * 查詢邏輯現在是針對 EmployeeUser 實體。
     * @param name 搜尋的姓名關鍵字
     * @return 匹配的 EmployeeUser 列表
     */
    // 3. 將查詢的實體從 EmployeeHr 改為 EmployeeUser
    @Query("SELECT e FROM EmployeeUser e WHERE CONCAT(e.lastName, ' ', e.firstName) LIKE %:name% OR CONCAT(e.firstName, ' ', e.lastName) LIKE %:name%")
    List<EmployeeUser> findByNameContainingIgnoreCase(@Param("name") String name);
}
