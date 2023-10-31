unit indented;

interface

uses
  SysUtils,
  System;

type
  TMyObject = class(TObject)
  end;

procedure MyProc;

implementation

uses
  StrUtils,
  Math;

type
  TMyOtherObject = class(TObject)
  private
    class procedure MyClassProc;
  end;

class procedure TMyOtherObject.MyClassProc;
begin
  Writeln('Hello world');
end;

procedure MyProc;
  procedure MySubProc;
  begin
  end;
var
  Anon: TProc;
begin
  Anon :=
    procedure begin
    end;
end;

initialization

finalization

end.