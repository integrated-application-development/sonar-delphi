unit DuplicatesTest;

interface

uses
  Classes,   // Unit containing the tstringlist command
  Windows;

type
  TBlah = class(TForm)
  public
    procedure foo;
  end;
var
  i : Integer;

implementation

procedure  TBlah.foo;
var
  TestList : tstringlist;

begin
  // Noncompliant: no sort; will cause out of bounds exception that is caught
  Blah.Duplicates := dupError;

  // Noncompliant: no sort
  TestList := TStringList.Create;
  TestList.Duplicates := dupError;

  // Noncompliant: sort wrong list
  DifferentTestList := TStringList.Create;
  TestList.Sorted := True;
  DifferentTestList.Duplicates := dupError;

  // Compliant
  TestList := TStringList.Create;
  TestList.Sorted := True;
  TestList.Duplicates := dupError;

end;

end.
