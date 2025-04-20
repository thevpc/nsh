package net.thevpc.nsh.history;

import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;

import java.io.*;

public abstract class AbstractNshHistory implements NshHistory {

    @Override
    public void load(NPath reader) {
        try {
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(reader.getInputStream()));
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    add(line);
                }
            } finally {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            }
        } catch (IOException e) {
            throw new NIOException(e);
        }
    }

    @Override
    public void load(Reader reader) {
        try {
            BufferedReader bufferedReader = null;
            try {
                if (reader instanceof BufferedReader) {
                    bufferedReader = (BufferedReader) reader;
                } else {
                    bufferedReader = new BufferedReader(reader);
                }
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    add(line);
                }
            } finally {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(NPath file) {
        if (file == null) {
            return;
        }
        if (file.getParent() != null) {
            file.getParent().mkdirs();
        }
        PrintWriter w = null;
        try {
            w = new PrintWriter(new OutputStreamWriter(file.getOutputStream()));
            save(w);
        } finally {
            if (w != null) {
                w.close();
            }
        }
    }

    @Override
    public void save(PrintWriter writer) {
        for (String element : getElements()) {
            writer.println(element);
        }
    }

    @Override
    public void save(PrintStream writer) {
        for (String element : getElements()) {
            writer.println(element);
        }
    }


    @Override
    public void append(NshHistory other) {
        for (String element : other.getElements(-1)) {
            add(element);
        }
    }
}
