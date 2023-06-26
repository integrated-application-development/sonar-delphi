unit ArrayWithAnonymousMethods;

interface

implementation

uses System.Generics.Collections;

procedure Test;
var
  MyArr: TArray<string>;
begin
  MyArr := [
    procedure begin
    end
  ];
end;

end.