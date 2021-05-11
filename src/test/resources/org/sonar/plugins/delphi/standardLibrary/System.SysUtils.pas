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

