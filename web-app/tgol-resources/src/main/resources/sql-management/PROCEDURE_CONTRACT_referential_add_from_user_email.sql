-- -----------------------------------------------------------------
-- Add referential to contracts
--   Contracts are identified by the user email they are associated with
--   Referential is identified by its id
-- -----------------------------------------------------------------
DROP PROCEDURE IF EXISTS add_ref_to_contract_from_user_email;

DELIMITER |
CREATE
    DEFINER =`asqatasun`@`localhost` PROCEDURE `add_ref_to_contract_from_user_email`
    (
        IN userEmail VARCHAR(191),
        IN refId     INT
    )
BLOCK1:
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE v_Id_Contract BIGINT(20);
    DECLARE contracts CURSOR FOR SELECT
                                     tc.Id_Contract
                                 FROM
                                     TGSI_CONTRACT tc
                                         LEFT JOIN TGSI_USER AS tu ON (tc.USER_Id_User = tu.Id_User)
                                 WHERE
                                     tu.Email1 = userEmail;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    OPEN contracts;
    LOOP1:
        LOOP
            FETCH contracts INTO v_Id_Contract;
            IF done = 1 THEN
                LEAVE LOOP1;
            END IF;
            INSERT IGNORE INTO
                `TGSI_CONTRACT_REFERENTIAL`
                (`CONTRACT_Id_Contract`, `REFERENTIAL_Id_Referential`)
            VALUES (v_Id_Contract, refId);
        END LOOP LOOP1;
END BLOCK1 |
DELIMITER ;
