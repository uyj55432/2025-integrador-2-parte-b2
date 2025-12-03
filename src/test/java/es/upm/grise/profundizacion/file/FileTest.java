
package es.upm.grise.profundizacion.file;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import es.upm.grise.profundizacion.exceptions.EmptyBytesArrayException;
import es.upm.grise.profundizacion.exceptions.InvalidContentException;
import es.upm.grise.profundizacion.exceptions.WrongFileTypeException;

public class FileTest {

    // -------------------------
    // Tests para addProperty()
    // -------------------------

    @Test
    void addProperty_nullContent_throwsInvalidContentException() {
        File file = new File();
        file.setType(FileType.PROPERTY);

        assertThrows(InvalidContentException.class, () -> file.addProperty(null));
    }

    @Test
    void addProperty_whenTypeImage_throwsWrongFileTypeException() {
        File file = new File();
        file.setType(FileType.IMAGE);
        char[] pair = "DATE=20250919".toCharArray();

        assertThrows(WrongFileTypeException.class, () -> file.addProperty(pair));
    }

    @Test
    void addProperty_appendsCharactersToContent() throws InvalidContentException, WrongFileTypeException {
        File file = new File();
        file.setType(FileType.PROPERTY);

        file.addProperty("A=1".toCharArray());
        file.addProperty(";B=2".toCharArray());

        List<Character> expected = new ArrayList<>();
        for (char c : "A=1;B=2".toCharArray()) {
            expected.add(c);
        }

        assertEquals(expected, file.getContent());
    }

    // -------------------------
    // Tests para getCRC32()
    // -------------------------

    @Test
    void getCRC32_emptyContent_returnsZero() throws EmptyBytesArrayException {
        File file = new File(); // content inicialmente vacío

        long crc = file.getCRC32();

        assertEquals(0L, crc);
    }

    @Test
    void getCRC32_emptyContent_doesNotConstructFileUtils() throws EmptyBytesArrayException {
        File file = new File();

        try (MockedConstruction<FileUtils> mocked = Mockito.mockConstruction(FileUtils.class)) {
            long crc = file.getCRC32();

            assertEquals(0L, crc);
            // Verifica que NO se construyó FileUtils cuando el contenido está vacío
            assertTrue(mocked.constructed().isEmpty());
        }
    }

    @Test
    void getCRC32_nonEmpty_delegatesWithBigEndianBytes()
            throws InvalidContentException, WrongFileTypeException, EmptyBytesArrayException {

        File file = new File();
        file.setType(FileType.PROPERTY);

        // Usamos dos caracteres: 'A' (0x0041) y '€' (U+20AC = 0x20AC) para verificar MSB/LSB
        char[] data = new char[] { 'A', '\u20AC' };
        file.addProperty(data);

        // Esperado: big-endian por char -> [MSB, LSB] de cada carácter
        byte[] expected = new byte[] {
                (byte) 0x00, (byte) 0x41,       // 'A'
                (byte) 0x20, (byte) 0xAC        // '€'
        };

        try (MockedConstruction<FileUtils> mocked = Mockito.mockConstruction(
                FileUtils.class,
                (mock, context) -> Mockito.when(mock.calculateCRC32(Mockito.any())).thenReturn(123L))) {

            long crc = file.getCRC32(); // dispara la construcción y la llamada al mock

            // Capturamos el argumento real enviado a calculateCRC32(byte[])
            FileUtils created = mocked.constructed().get(0);
            ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
            Mockito.verify(created).calculateCRC32(captor.capture());

            assertArrayEquals(expected, captor.getValue());
            // (Este test se centra en la transformación y delegación; no comprobamos aquí el valor devuelto)
        }
    }

    @Test
    void getCRC32_nonEmpty_returnsValueProvidedByFileUtils()
            throws InvalidContentException, WrongFileTypeException, EmptyBytesArrayException {

        File file = new File();
        file.setType(FileType.PROPERTY);
        file.addProperty("X".toCharArray()); // cualquier contenido no vacío

        long expectedCrc = 987_654_321L;

        try (MockedConstruction<FileUtils> mocked = Mockito.mockConstruction(
                FileUtils.class,
                (mock, context) -> Mockito.when(mock.calculateCRC32(Mockito.any())).thenReturn(expectedCrc))) {

            long crc = file.getCRC32();

            assertEquals(expectedCrc, crc);
        }
    }
}
