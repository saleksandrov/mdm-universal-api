package com.asv.upload.parser;

import com.asv.unapi.service.model.Item;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author alexandrov
 * @since 07.09.2016
 */
public interface ParserAction {

    List<Item> parse(InputStream archiveInputStream) throws IOException;

}
