package com.github.basdxz.vnetfetch.util;

import lombok.experimental.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.github.basdxz.vnetfetch.util.Constants.NAME;


@UtilityClass
public final class LoggingUtil {
    private static final Logger LOGGER = LogManager.getLogger(NAME);

    public static Logger log() {
        return LOGGER;
    }
}
