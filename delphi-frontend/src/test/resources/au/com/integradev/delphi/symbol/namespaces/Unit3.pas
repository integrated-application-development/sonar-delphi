unit Unit3;

interface

procedure Unit3Proc(Argument: String);

implementation

uses
    Namespaced.Unit1
  , Namespaced.Unit2
  ;

procedure Unit3Proc(Argument: String);
begin
  // Do nothing
end;

end.