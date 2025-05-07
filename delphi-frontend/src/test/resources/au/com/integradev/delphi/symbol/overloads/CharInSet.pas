unit CharInSet;

interface

type
  TCharSet = set of AnsiChar;
  TSysCharSet = set of AnsiChar;

  TFoo = class(TObject)
  protected
    FChar: Char;
  public
    property CharProperty: Char read FChar;
    function Test(TokenChar: TCharSet): Boolean;
  end;

function CharInSet(C: AnsiChar; const CharSet: TSysCharSet): Boolean; overload; inline;
function CharInSet(C: WideChar; const CharSet: TSysCharSet): Boolean; overload; inline;

implementation


function TFoo.Test(TokenChar: TCharSet): Boolean;
begin
  Result := CharInSet(CharProperty, TokenChar);
end;

end.