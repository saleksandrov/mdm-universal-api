package com.asv.upload;

import com.asv.upload.parser.ParserAction;
import com.asv.upload.worker.ImportTask;

/**
 * @author alexandrov
 * @since 07.09.2016
 */
public enum Table {

    // add configuration params
    NULL(null, null);

    ParserAction parserAction;
    ImportTask importTask;

    Table(com.asv.upload.parser.ParserAction parserAction, ImportTask importTask) {
        this.parserAction = parserAction;
        this.importTask = importTask;
    }

}
