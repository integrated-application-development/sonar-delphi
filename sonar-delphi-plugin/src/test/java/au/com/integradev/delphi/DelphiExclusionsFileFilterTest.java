package au.com.integradev.delphi;

import static org.assertj.core.api.Assertions.*;

import au.com.integradev.delphi.core.Delphi;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFileFilter;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.config.internal.MapSettings;

class DelphiExclusionsFileFilterTest {
  @Test
  void testShouldAcceptNonExcludedFile() {
    MapSettings settings = new MapSettings();

    settings.setProperty(
        DelphiProperties.EXCLUSIONS_KEY, DelphiProperties.EXCLUSIONS_DEFAULT_VALUE);
    InputFileFilter filter = new DelphiExclusionsFileFilter(settings.asConfig());

    assertThat(filter.accept(inputFile("file.pas"))).isTrue();
  }

  @Test
  void testShouldExcludeDefaults() {
    MapSettings settings = new MapSettings();

    settings.setProperty(
        DelphiProperties.EXCLUSIONS_KEY, DelphiProperties.EXCLUSIONS_DEFAULT_VALUE);
    InputFileFilter filter = new DelphiExclusionsFileFilter(settings.asConfig());

    assertThat(filter.accept(inputFile("__history/file.pas"))).isFalse();
    assertThat(filter.accept(inputFile("__history/foo/file.pas"))).isFalse();
    assertThat(filter.accept(inputFile("foo/__history/file.pas"))).isFalse();
    assertThat(filter.accept(inputFile("__recovery/file.pas"))).isFalse();
    assertThat(filter.accept(inputFile("__recovery/foo/file.pas"))).isFalse();
    assertThat(filter.accept(inputFile("foo/__recovery/file.pas"))).isFalse();
  }

  @Test
  void testShouldOnlyExcludeDelphi() {
    MapSettings settings = new MapSettings();

    settings.setProperty(
        DelphiProperties.EXCLUSIONS_KEY, DelphiProperties.EXCLUSIONS_DEFAULT_VALUE);
    InputFileFilter filter = new DelphiExclusionsFileFilter(settings.asConfig());

    InputFile jsonFile =
        new TestInputFileBuilder("test", "test/__history/non_delphi_file.json")
            .setLanguage("json")
            .build();

    assertThat(filter.accept(jsonFile)).isTrue();
  }

  @Test
  void testShouldAcceptDefaultExclusionsWhenOverridden() {
    MapSettings settings = new MapSettings();

    settings.setProperty(DelphiProperties.EXCLUSIONS_KEY, "");
    InputFileFilter filter = new DelphiExclusionsFileFilter(settings.asConfig());

    assertThat(filter.accept(inputFile("file.pas"))).isTrue();
    assertThat(filter.accept(inputFile("__history/file.pas"))).isTrue();
    assertThat(filter.accept(inputFile("__recovery/file.pas"))).isTrue();
  }

  @Test
  void testShouldExcludeUsingCustomPattern() {
    MapSettings settings = new MapSettings();

    settings.setProperty(DelphiProperties.EXCLUSIONS_KEY, "**/foo/**");
    InputFileFilter filter = new DelphiExclusionsFileFilter(settings.asConfig());

    assertThat(filter.accept(inputFile("file.pas"))).isTrue();
    assertThat(filter.accept(inputFile("__history/file.pas"))).isTrue();
    assertThat(filter.accept(inputFile("__recovery/file.pas"))).isTrue();
    assertThat(filter.accept(inputFile("foo/file.pas"))).isFalse();
    assertThat(filter.accept(inputFile("foo/bar/file.pas"))).isFalse();
    assertThat(filter.accept(inputFile("bar/foo/file.pas"))).isFalse();
  }

  @Test
  void testShouldIgnoreEmptyElements() {
    MapSettings settings = new MapSettings();
    settings.setProperty(
        DelphiProperties.EXCLUSIONS_KEY, "," + DelphiProperties.EXCLUSIONS_DEFAULT_VALUE + ",");
    InputFileFilter filter = new DelphiExclusionsFileFilter(settings.asConfig());

    assertThat(filter.accept(inputFile("file.pas"))).isTrue();
    assertThat(filter.accept(inputFile("__history/file.pas"))).isFalse();
    assertThat(filter.accept(inputFile("__recovery/file.pas"))).isFalse();
  }

  private static InputFile inputFile(String file) {
    return new TestInputFileBuilder("test", "test/" + file).setLanguage(Delphi.KEY).build();
  }
}
