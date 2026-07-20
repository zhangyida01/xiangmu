package com.delivery.management.controller;

import com.delivery.management.common.Result;
import com.delivery.management.entity.ProjectAccount;
import com.delivery.management.service.ProjectAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/project-account")
public class ProjectAccountController {

    @Autowired
    private ProjectAccountService projectAccountService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 鏍规嵁椤圭洰ID鑾峰彇璐︽埛鍒楄〃
     */
    @GetMapping("/list/{projectId}")
    public Result<List<ProjectAccount>> listByProject(@PathVariable Long projectId) {
        List<ProjectAccount> accounts = projectAccountService.getByProjectId(projectId);
        return Result.success(accounts);
    }

    /**
     * 鑾峰彇璐︽埛璇︽儏锛堥渶瑕佽В瀵嗗瘑鐮侊級
     */
    @GetMapping("/detail/{id}")
    public Result<ProjectAccount> detail(@PathVariable Long id) {
        ProjectAccount account = projectAccountService.getById(id);
        if (account == null) {
            return Result.fail("璐︽埛涓嶅瓨鍦?);
        }
        // 娉ㄦ剰锛氳繑鍥炵粰鍓嶇鏃朵笉鍖呭惈瀵嗙爜锛岄渶瑕佸崟鐙姹?        account.setPassword("******");
        return Result.success(account);
    }
    
    /**
     * 鑾峰彇璐︽埛鐪熷疄瀵嗙爜锛堥渶瑕佺壒娈婃潈闄愶級
     */
    @GetMapping("/password/{id}")
    public Result<String> getPassword(@PathVariable Long id) {
        ProjectAccount account = projectAccountService.getById(id);
        if (account == null) {
            return Result.fail("璐︽埛涓嶅瓨鍦?);
        }
        // 杩欓噷绠€鍗曡繑鍥烇紝瀹為檯搴旇鍔犳潈闄愰獙璇佸拰鎿嶄綔鏃ュ織
        return Result.success(account.getPassword());
    }

    /**
     * 娣诲姞椤圭洰璐︽埛
     */
    @PostMapping("/add")
    public Result<String> add(@RequestBody ProjectAccount account) {
        // 鍔犲瘑瀵嗙爜锛堜娇鐢˙ase64绠€鍗曞姞瀵嗭紝瀹為檯椤圭洰鍙敤AES绛夛級
        if (account.getPassword() != null && !account.getPassword().isEmpty()) {
            // 杩欓噷涓轰簡婕旂ず锛屼娇鐢˙Crypt鍔犲瘑锛屽疄闄呭缓璁敤鍙€嗗姞瀵?            account.setPassword(passwordEncoder.encode(account.getPassword()));
        }
        
        boolean success = projectAccountService.save(account);
        return success ? Result.success("娣诲姞鎴愬姛") : Result.fail("娣诲姞澶辫触");
    }

    /**
     * 鏇存柊椤圭洰璐︽埛
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody ProjectAccount account) {
        if (account.getId() == null) {
            return Result.fail("璐︽埛ID涓嶈兘涓虹┖");
        }
        
        // 濡傛灉瀵嗙爜鏈夋洿鏂帮紝閲嶆柊鍔犲瘑
        if (account.getPassword() != null && !account.getPassword().isEmpty() && !account.getPassword().equals("******")) {
            account.setPassword(passwordEncoder.encode(account.getPassword()));
        } else {
            // 涓嶆洿鏂板瘑鐮佸瓧娈?            account.setPassword(null);
        }
        
        boolean success = projectAccountService.updateById(account);
        return success ? Result.success("鏇存柊鎴愬姛") : Result.fail("鏇存柊澶辫触");
    }

    /**
     * 鍒犻櫎椤圭洰璐︽埛
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = projectAccountService.removeById(id);
        return success ? Result.success("鍒犻櫎鎴愬姛") : Result.fail("鍒犻櫎澶辫触");
    }
}
