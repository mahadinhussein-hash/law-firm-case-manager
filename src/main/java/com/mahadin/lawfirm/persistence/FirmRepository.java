package com.mahadin.lawfirm.persistence;

import java.io.IOException;

/**
 * Where the firm's data is stored. The rest of the program only knows this
 * interface, so the storage format (JSON file, Java serialization, ...) can be
 * changed without touching the service or the user interfaces.
 */
public interface FirmRepository {

    void save(FirmData data) throws IOException;

    FirmData load() throws IOException;
}
