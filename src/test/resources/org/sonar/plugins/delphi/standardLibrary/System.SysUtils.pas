{*******************************************************}
{               System Utilities Unit                   }
{*******************************************************}

unit System.SysUtils;

interface

procedure FreeAndNil(var Obj); inline;

type
  TStringHelper = record helper for string
  public
    function IsEmpty: Boolean;
  end;

  TFormatSettings = record
    type
      TEraInfo = record
        EraName: string;
        EraOffset: Integer;
        EraStart: TDate;
        EraEnd: TDate;
      end;
    public
      CurrencyString: string;
      CurrencyFormat: Byte;
      CurrencyDecimals: Byte;
      DateSeparator: Char;
      TimeSeparator: Char;
      ListSeparator: Char;
      ShortDateFormat: string;
      LongDateFormat: string;
      TimeAMString: string;
      TimePMString: string;
      ShortTimeFormat: string;
      LongTimeFormat: string;
      ShortMonthNames: array[1..12] of string;
      LongMonthNames: array[1..12] of string;
      ShortDayNames: array[1..7] of string;
      LongDayNames: array[1..7] of string;
      EraInfo: array of TEraInfo;
      ThousandSeparator: Char;
      DecimalSeparator: Char;
      TwoDigitYearCenturyWindow: Word;
      NegCurrFormat: Byte;
      NormalizedLocaleName: string;

      class function Create: TFormatSettings; overload; static; inline;
      class function Create(Locale: TLocaleID): TFormatSettings; overload; platform; static;
      class function Create(const LocaleName: string): TFormatSettings; overload; static;
      class function Invariant: TFormatSettings; static;
  end;

  function StrToDateTime(const S: string): TDateTime; overload; inline;
  function StrToDateTime(const S: string;
    const AFormatSettings: TFormatSettings): TDateTime; overload;

implementation

end.

