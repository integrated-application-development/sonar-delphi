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

implementation

end.

