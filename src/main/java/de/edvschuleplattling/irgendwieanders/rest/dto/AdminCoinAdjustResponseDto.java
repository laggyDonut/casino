package de.edvschuleplattling.irgendwieanders.rest.dto;

import de.edvschuleplattling.irgendwieanders.model.wallet.Wallet;
import lombok.Data;

@Data
public class AdminCoinAdjustResponseDto {

    private Long userId;
    private Long walletId;
    private long amountDelta;
    private long newBalance;

    public static AdminCoinAdjustResponseDto fromEntity(Long userId, Wallet wallet, long amountDelta) {
        AdminCoinAdjustResponseDto dto = new AdminCoinAdjustResponseDto();
        dto.setUserId(userId);
        dto.setWalletId(wallet.getId());
        dto.setAmountDelta(amountDelta);
        dto.setNewBalance(wallet.getBalance());
        return dto;
    }
}
