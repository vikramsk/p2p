package p2p;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// FileHandler manages access to the file used
// by a peer for handling data transfer.
class FileHandler {
	
	// pieceSize defines the size of a piece in bytes.
	int pieceSize;

	// path where the file is stored.
	String path;

	// fileLock makes the operations on the file thread safe.
	private final ReadWriteLock fileLock = new ReentrantReadWriteLock();
	
	// file is the underlying file used by a peer for data transfer.
	RandomAccessFile file;

	// FileHandler defines the constructor for creating
	// the file handler.
	// The file is opened in "rw" mode by default.
	FileHandler(String path, boolean readOnly, int pieceSize) {
		this.path = path;
		this.pieceSize = pieceSize;
		try {
			if (readOnly)
				file = new RandomAccessFile(path, "r");
			else {
				file = new RandomAccessFile(path, "rw");
				file.setLength(Configs.Common.FileSize);
			}		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// getPiece returns the requested piece using the specified
	// pieceIndex.
	// This operation is thread safe.
	public byte[] getPiece(int pieceIndex) throws IOException {
		byte[] data = new byte[pieceSize];
		fileLock.readLock().lock();
		try {
			file.seek(pieceIndex*pieceSize);
			file.readFully(data);
		} finally {
			fileLock.readLock().unlock();
		}
		return data;
	}

	// addPiece adds the given piece of data at the specified piece
	// pieceIndex.
	// This assumes that the data block adheres to the pieceSize.
	// This operation is thread safe.
	public boolean addPiece(int pieceIndex, byte[] data) throws IOException {
		boolean status = false;
		fileLock.writeLock().lock();
		try {
			file.seek(pieceIndex*pieceSize);
			file.write(data);
			status = true;
		} finally {
			fileLock.writeLock().unlock();
		}
		return status;
	}
}
