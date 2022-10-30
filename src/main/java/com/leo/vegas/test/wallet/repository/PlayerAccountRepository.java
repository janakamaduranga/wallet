package com.leo.vegas.test.wallet.repository;

import com.leo.vegas.test.wallet.entity.PlayerAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerAccountRepository extends JpaRepository<PlayerAccount, String> {
}
