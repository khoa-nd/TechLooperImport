package com.techlooper.suggestion;

import com.techlooper.repository.VietnamworksUserRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by NguyenDangKhoa on 6/1/15.
 */
@Service
public class JobTitleReader {

    @Resource
    private VietnamworksUserRepository vietnamworksUserRepository;

    public List<String> readJobTitle(int fromIndex, int size) {
        return vietnamworksUserRepository.getJobTitles(fromIndex, size);
    }

    public int getTotalNumberOfRegistrationUsers() {
        return vietnamworksUserRepository.getTotalNumberOfRegistrationUsers();
    }
}
