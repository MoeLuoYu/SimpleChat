// ???????
Verify verify = new Verify();
        LoggerUtils.redisChat("????????...");
        LoggerUtils.redisChat("????????? VerifyCube...");
        Verify.verifyCubeInfo("Loading server plugin VerifyCube v1.0");
        Verify.verifyCubeInfo("Enabling VerifyCube v1.0");
boolean verifySuccess = verify.verify();
        if (!verifySuccess) {
        LoggerUtils.redisChat("?????????????Redis?????");
            LoggerUtils.redisChat("???????? VerifyCube...");
            Verify.verifyCubeInfo("Disabling VerifyCube v1.0");
            Verify.verifyCubeInfo("VerifyCube ????");
            return;
                    } else {
                    LoggerUtils.redisChat("?????????????Redis?????");
            LoggerUtils.redisChat("???????? VerifyCube...");
            Verify.verifyCubeInfo("Disabling VerifyCube v1.0");
            Verify.verifyCubeInfo("VerifyCube ????");
        }